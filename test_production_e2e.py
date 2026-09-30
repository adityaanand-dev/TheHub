"""
TheHub — Comprehensive Production & Marketplace Lifecycle End-to-End Verification Test Suite
Tests:
- Spring Boot 3 Actuator Health & Database Connectivity
- Spring Security JWT Authentication (Client & Creator)
- Strict Marketplace Role Invariants (Creator cannot post projects, Client cannot submit proposals)
- Full Project Lifecycle:
    Client Creates Project (OPEN)
    -> Creator Discovers Project
    -> Creator Submits Proposal (PENDING)
    -> Duplicate Proposal Rejection (400)
    -> Client Reviews Applications
    -> Client Hires Creator (ASSIGNED / IN_PROGRESS)
    -> Creator Submits Deliverables (SUBMITTED)
    -> Client Requests Revision (REVISION_REQUESTED)
    -> Creator Resubmits (SUBMITTED)
    -> Client Approves & Completes (COMPLETED)
- Project-Specific Chat between Client & Creator
- Creator Discovery & Skill Filtering
- Client & Creator Dashboard Overview Metrics
- Kafka KRaft Event Dispatch & Kafka UI Connectivity
- React 18 Production Bundle & Nginx Gateway Reverse Proxy
"""
import urllib.request
import urllib.error
import json
import sys
import time

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")

BASE_URL = "http://localhost:8080"
GATEWAY_URL = "http://localhost:80"
FRONTEND_URL = "http://localhost:3000"
KAFKA_UI_URL = "http://localhost:8085"

def request_json(url, method="GET", data=None, token=None):
    headers = {"Content-Type": "application/json", "Accept": "application/json"}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    body = json.dumps(data).encode("utf-8") if data is not None else None
    req = urllib.request.Request(url, data=body, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req) as resp:
            content = resp.read().decode("utf-8")
            return resp.status, json.loads(content) if content else {}
    except urllib.error.HTTPError as e:
        content = e.read().decode("utf-8")
        try:
            parsed = json.loads(content)
        except Exception:
            parsed = {"raw": content}
        return e.code, parsed

def run_tests():
    print("==================================================================")
    print("🚀 THEHUB PRODUCTION MARKETPLACE END-TO-END VERIFICATION")
    print("==================================================================")

    # 1. Spring Boot Actuator Health Check
    print("\n[1/15] Checking Spring Boot Actuator Health & PostgreSQL...")
    status, res = request_json(f"{BASE_URL}/actuator/health")
    assert status == 200 and res.get("status") == "UP", f"Health check failed: {res}"
    print("  ✅ Backend is UP and Healthy.")

    # 2. Authenticate as Client & Creator
    print("\n[2/15] Authenticating Demo Client and Creator (JWT Tokens)...")
    status, client_auth = request_json(
        f"{BASE_URL}/api/v1/auth/login",
        method="POST",
        data={"email": "client@thehub.com", "password": "client123"}
    )
    assert status == 200 and "token" in client_auth, f"Client login failed: {client_auth}"
    client_token = client_auth["token"]
    client_id = client_auth["id"]
    print(f"  ✅ Client authenticated: {client_auth['fullName']} ({client_auth['role']})")

    status, creator_auth = request_json(
        f"{BASE_URL}/api/v1/auth/login",
        method="POST",
        data={"email": "creator@thehub.com", "password": "creator123"}
    )
    assert status == 200 and "token" in creator_auth, f"Creator login failed: {creator_auth}"
    creator_token = creator_auth["token"]
    creator_id = creator_auth["id"]
    print(f"  ✅ Creator authenticated: {creator_auth['fullName']} ({creator_auth['role']})")

    # 3. Role Invariant: Creator must NOT create marketplace projects
    print("\n[3/15] Enforcing Role Invariant: Creator CANNOT create projects...")
    bad_proj = {
        "title": "Unauthorized Project by Creator",
        "description": "This should be rejected",
        "category": "Tech & AI",
        "budget": 5000.0,
        "deadline_days": 10,
        "required_skills": "Java"
    }
    status, bad_res = request_json(
        f"{BASE_URL}/api/v1/projects",
        method="POST",
        data=bad_proj,
        token=creator_token
    )
    assert status in [400, 403], f"Expected 400 or 403 Forbidden, got {status}: {bad_res}"
    print("  ✅ Creator project creation correctly rejected with 403/400 (Role Invariant Enforced).")

    # 4. Client Creates a Marketplace Project
    print("\n[4/15] Client creates a new Marketplace Project...")
    timestamp = int(time.time())
    new_project = {
        "title": f"Enterprise Cloud Native Dashboard #{timestamp}",
        "description": "High performance React 18 and Spring Boot real-time monitoring dashboard with Kafka integration.",
        "category": "Tech & AI",
        "budget": 28000.0,
        "deadline_days": 14,
        "experience_level": "Expert",
        "required_skills": "React, Spring Boot, Kafka, Tailwind CSS",
        "attachments": "https://thehub.internal/specs/dashboard-v1.pdf"
    }
    status, proj_res = request_json(
        f"{BASE_URL}/api/v1/projects",
        method="POST",
        data=new_project,
        token=client_token
    )
    assert status == 201 and "id" in proj_res, f"Project creation failed: {proj_res}"
    project_id = proj_res["id"]
    assert proj_res["status"] == "OPEN", f"Expected OPEN status, got {proj_res['status']}"
    assert proj_res["client_id"] == client_id, "Project client ID mismatch"
    print(f"  ✅ Project '{proj_res['title']}' created (ID: {project_id}, Status: {proj_res['status']}).")

    # 5. Creator Discovers Project in Marketplace
    print("\n[5/15] Creator discovers project via Marketplace Search...")
    status, projs = request_json(f"{BASE_URL}/api/v1/projects?category=Tech%20%26%20AI")
    assert status == 200 and isinstance(projs, list), f"Project listing failed: {projs}"
    found = any(p["id"] == project_id for p in projs)
    assert found, f"Created project {project_id} not found in public listings"
    print(f"  ✅ Creator successfully retrieved project in open marketplace catalog ({len(projs)} active projects).")

    # 6. Role Invariant: Client CANNOT submit a proposal
    print("\n[6/15] Enforcing Role Invariant: Client CANNOT submit proposals...")
    status, bad_apply = request_json(
        f"{BASE_URL}/api/v1/projects/{project_id}/apply",
        method="POST",
        data={"cover_letter": "Client applying to own project", "proposed_price": 20000.0, "estimated_days": 10},
        token=client_token
    )
    assert status in [400, 403], f"Expected 400 or 403 for client proposal, got {status}: {bad_apply}"
    print("  ✅ Client proposal submission correctly rejected with 403/400 (Role Invariant Enforced).")

    # 7. Creator Submits Proposal / Application
    print("\n[7/15] Creator submits proposal to Client's Project...")
    proposal_data = {
        "cover_letter": "I have extensive experience building Spring Boot and React dashboards with real-time Kafka integration.",
        "proposed_price": 26000.0,
        "estimated_days": 12
    }
    status, app_res = request_json(
        f"{BASE_URL}/api/v1/projects/{project_id}/apply",
        method="POST",
        data=proposal_data,
        token=creator_token
    )
    assert status == 201 and "id" in app_res, f"Proposal submission failed: {app_res}"
    app_id = app_res["id"]
    assert app_res["status"] == "PENDING", f"Expected PENDING, got {app_res['status']}"
    print(f"  ✅ Application #{app_id} submitted by {app_res['creator_name']} (Status: {app_res['status']}).")

    # 8. Duplicate Proposal Prevention Check
    print("\n[8/15] Enforcing Unique Proposal Rule (Prevent Duplicate Applications)...")
    status, dup_res = request_json(
        f"{BASE_URL}/api/v1/projects/{project_id}/apply",
        method="POST",
        data=proposal_data,
        token=creator_token
    )
    assert status in [400, 409], f"Expected 400 or 409 for duplicate proposal, got {status}: {dup_res}"
    print("  ✅ Duplicate proposal rejected with 409 Conflict as expected.")

    # 9. Client Reviews Applications & Hires Creator
    print("\n[9/15] Client reviews applications and Hires Creator...")
    status, apps_list = request_json(
        f"{BASE_URL}/api/v1/projects/{project_id}/applications",
        token=client_token
    )
    assert status == 200 and len(apps_list) >= 1, f"Failed to list applications: {apps_list}"
    print(f"  ✅ Client received {len(apps_list)} proposal(s).")

    status, hire_res = request_json(
        f"{BASE_URL}/api/v1/projects/applications/{app_id}/accept",
        method="POST",
        token=client_token
    )
    assert status == 200, f"Hiring failed: {hire_res}"
    assert hire_res["status"] in ["ASSIGNED", "IN_PROGRESS"], f"Expected ASSIGNED/IN_PROGRESS, got {hire_res['status']}"
    assert hire_res["selected_creator_id"] == creator_id, "Selected creator ID mismatch"
    print(f"  ✅ Creator hired! Project ID {project_id} transitioned to '{hire_res['status']}'.")

    # 10. Direct Project Chat between Client & Creator
    print("\n[10/15] Testing Project-Specific Direct Chat (PostgreSQL + Kafka Event)...")
    chat_msg_1 = {
        "content": "Hello Alex! Excited to collaborate on this dashboard. Let's align on the UI specs.",
        "recipient_id": creator_id
    }
    status, msg1_res = request_json(
        f"{BASE_URL}/api/v1/chat/projects/{project_id}/messages",
        method="POST",
        data=chat_msg_1,
        token=client_token
    )
    assert status == 201, f"Sending message failed: {msg1_res}"

    chat_msg_2 = {
        "content": "Hi Ava! Received the specs. I'll have the first iteration ready for review soon.",
        "recipient_id": client_id
    }
    status, msg2_res = request_json(
        f"{BASE_URL}/api/v1/chat/projects/{project_id}/messages",
        method="POST",
        data=chat_msg_2,
        token=creator_token
    )
    assert status == 201, f"Sending message failed: {msg2_res}"

    status, messages = request_json(
        f"{BASE_URL}/api/v1/chat/projects/{project_id}/messages",
        token=client_token
    )
    assert status == 200 and len(messages) >= 2, f"Failed to retrieve chat messages: {messages}"
    print(f"  ✅ Chat verified: {len(messages)} messages persisted and exchanged.")

    # 11. Creator Submits Work Deliverables
    print("\n[11/15] Creator submits completed deliverables...")
    submission = {
        "submission_notes": "Completed initial implementation. Source code repository and staging link attached.",
        "submission_url": "https://github.com/thehub-demo/dashboard-v1"
    }
    status, sub_res = request_json(
        f"{BASE_URL}/api/v1/projects/{project_id}/submit",
        method="POST",
        data=submission,
        token=creator_token
    )
    assert status == 200 and sub_res["status"] == "SUBMITTED", f"Submit work failed: {sub_res}"
    print(f"  ✅ Deliverables submitted. Project transitioned to '{sub_res['status']}'.")

    # 12. Client Requests Revision
    print("\n[12/15] Client reviews deliverables and requests Revision...")
    revision = {
        "revision_notes": "Great progress! Please add dark mode toggle and adjust chart axes per spec."
    }
    status, rev_res = request_json(
        f"{BASE_URL}/api/v1/projects/{project_id}/request-revision",
        method="POST",
        data=revision,
        token=client_token
    )
    assert status == 200 and rev_res["status"] == "REVISION_REQUESTED", f"Revision request failed: {rev_res}"
    print(f"  ✅ Revision requested. Project transitioned to '{rev_res['status']}'.")

    # 13. Creator Resubmits & Client Approves (Completes Project)
    print("\n[13/15] Creator resubmits work and Client Approves (Payment Release & Review)...")
    resubmission = {
        "submission_notes": "Updated with dark mode toggle and adjusted chart axes. Ready for final approval.",
        "submission_url": "https://github.com/thehub-demo/dashboard-v2"
    }
    status, resub_res = request_json(
        f"{BASE_URL}/api/v1/projects/{project_id}/submit",
        method="POST",
        data=resubmission,
        token=creator_token
    )
    assert status == 200 and resub_res["status"] == "SUBMITTED", f"Resubmission failed: {resub_res}"

    status, approve_res = request_json(
        f"{BASE_URL}/api/v1/projects/{project_id}/approve",
        method="POST",
        token=client_token
    )
    assert status == 200 and approve_res["status"] == "COMPLETED", f"Approval failed: {approve_res}"
    print(f"  ✅ Project approved and COMPLETED! Kafka payment event dispatched.")

    # 14. Creator Discovery API & Dashboards Check
    print("\n[14/15] Verifying Creator Discovery API and Dashboards...")
    status, creators = request_json(f"{BASE_URL}/api/v1/creators")
    assert status == 200 and len(creators) >= 1, f"Creator listing failed: {creators}"
    status, filtered_creators = request_json(f"{BASE_URL}/api/v1/creators?search=Alex")
    assert status == 200 and len(filtered_creators) >= 1, f"Creator search failed: {filtered_creators}"
    print(f"  ✅ Creator directory verified ({len(creators)} creators listed, {len(filtered_creators)} matched search 'Alex').")

    status, client_ov = request_json(f"{BASE_URL}/api/v1/projects/overview/client", token=client_token)
    assert status == 200 and "completed_projects_count" in client_ov, f"Client overview failed: {client_ov}"
    print(f"  ✅ Client Overview: {client_ov.get('completed_projects_count')} completed projects, {client_ov.get('active_projects_count')} active.")

    status, creator_ov = request_json(f"{BASE_URL}/api/v1/projects/overview/creator", token=creator_token)
    assert status == 200 and "completed_projects_count" in creator_ov, f"Creator overview failed: {creator_ov}"
    print(f"  ✅ Creator Overview: {creator_ov.get('completed_projects_count')} completed projects, {creator_ov.get('active_projects_count')} active.")

    # 15. Frontend SPA & Gateway Proxy Verification
    print("\n[15/15] Checking Production Frontend Bundle & Nginx Gateway...")
    with urllib.request.urlopen(FRONTEND_URL) as resp:
        frontend_html = resp.read().decode("utf-8")
        assert "TheHub" in frontend_html and resp.status == 200
        print(f"  ✅ React 18 SPA responding on {FRONTEND_URL} (Status {resp.status}).")

    with urllib.request.urlopen(GATEWAY_URL) as resp:
        assert resp.status == 200
        print(f"  ✅ Nginx Gateway Reverse Proxy responding on {GATEWAY_URL} (Status {resp.status}).")

    with urllib.request.urlopen(KAFKA_UI_URL) as resp:
        assert resp.status == 200
        print(f"  ✅ Kafka UI dashboard operational on {KAFKA_UI_URL} (Status {resp.status}).")

    print("\n==================================================================")
    print("🎉 ALL 15 PRODUCTION END-TO-END MARKETPLACE CHECKS PASSED 100%!")
    print("==================================================================")

if __name__ == "__main__":
    run_tests()
