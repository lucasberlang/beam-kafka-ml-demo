
variable "project_id" {
  description = "fixture"
  type        = string
}

variable "region" {
  description = "fixture"
  type        = string
  default     = "europe-west4"
}

######
# Bigquery
######

variable "id" {
  description = "Dataset id."
  type        = string
}

variable "offset_bq" {
  description = "The offset to be added to the bigquery dataset"
  type        = number
  default     = 1
}

######
# CloudStorage
######

variable "force_destroy" {
  description = "Optional map to set force destroy keyed by name, defaults to false."
  type        = bool
}

variable "offset" {
  description = "The offset to be added to the google cloud storage"
  type        = number
}

variable "name" {
  description = "Cloud storage name."
  type        = string
}

######
# Tags
######

variable "tags" {
  description = "fixture"
  type        = map(string)
}
