library "PhEMA Heart Failure" version '1.0.0'

using QUICK

codesystem "ActCodes": 'http://hl7.org/fhir/v3/ActCode'

valueset "Echo VS": '2.16.840.1.999999.1'
valueset "HF Dx VS": '2.16.840.1.113883.3.526.3.376'

code "Inpatient Encounter": 'IMP' from "ActCodes"
code "Outpatient Encounter": 'AMB' from "ActCodes"

context Patient

define "Adult":
    AgeInYears() >= 18

define "Has Echo":
    exists(["Procedure": "Echo VS"])

define "Has HF Dx":
    exists(["Condition": "HF Dx VS"])

define "Inpatient Encounters With HF Dx":
    [Encounter: "Inpatient Encounter"] E
        with [Condition: "HF Dx VS"] C
            such that E.id = C.encounter

define "Outpatient Encounters With HF Dx":
    [Encounter: "Outpatient Encounter"] E
        with [Condition: "HF Dx VS"] C
            such that E.id = C.encounter

define "Encounter Criteria":
    exists("Inpatient Encounters With HF Dx") or
    Count("Outpatient Encounters With HF Dx") >= 2

define "Case":
    "Adult" and "Has Echo" and "Encounter Criteria"
