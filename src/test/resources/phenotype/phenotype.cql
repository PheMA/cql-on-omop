library "Test Phenotype" version '1.0.0'

using QUICK

valueset "Diabetes VS": '1.2.3.4.5'
valueset "Hypertension VS": '2.3.4.5.6'
valueset "Afib VS": '3.4.5.6.7'

define "Phenotype Case":
  exists([Condition: "Diabetes VS"])
  and (exists([Condition: "Hypertension VS"]) or exists([Condition: "Afib VS"]))