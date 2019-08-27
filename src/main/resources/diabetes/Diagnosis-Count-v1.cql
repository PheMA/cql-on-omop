library "Diagnosis-Count" version '1'

using FHIR version '1.0.2'

valueset "Diabetes VS": '2.16.840.1.113883.3.464.1003.103.12.1001'

context Patient

define "Diabetes":
  Count([Condition: "Diabetes VS"]) > 2


define "Diabetes - Cases (Count > 2)":
  "Diabetes"
