terraform {
  required_version = ">= 1.5.0"

  required_providers {
    # Using generic cloud/local providers to remain cloud-agnostic
    local = {
      source  = "hashicorp/local"
      version = "~> 2.5"
    }
    template = {
      source  = "hashicorp/template"
      version = "~> 2.2"
    }
  }
}
