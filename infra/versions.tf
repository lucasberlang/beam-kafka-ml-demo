######
# REQUIRE A SPECIFIC TERRAFORM VERSION OR HIGHER
# This module has been updated with 0.15 syntax, which means it is no longer compatible with any versions below 0.12.
######
terraform {
  required_version = ">= 0.15"
  required_providers {
    google = "~> 4.0"
    google-beta = {
      source  = "hashicorp/google-beta"
      version = "~> 4.0"
    }
  }

  backend "gcs" {
    bucket  = "go-eu-bluetab-cloud-state-cs01-dev"
    prefix  = "beam-kafka-ml-demo"
  }

  experiments = [module_variable_optional_attrs]
}
