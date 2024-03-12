
project_id = "practica-cloud-286009"

######
# bigquery
######

id = "kafka_streaming_dataflow"

offset_bq = 1


######
# cloudstorage
######

force_destroy = true

offset = 1

name = "sink"

######
# Tags
######

tags = {
  "provider"                = "go",
  "region"                  = "euw4",
  "enterprise"              = "ing",
  "account"                 = "blue",
  "system"                  = "data"
  "environment"             = "tst",
  "security_exposure_level" = "mz",
}
