library "PhEMA Heart Failure" version '0.4.0'

using QUICK

valueset "Echo VS": '2.16.840.1.999999.1'
valueset "HF Dx VS": '2.16.840.1.113883.3.526.3.376'
valueset "Inpatient Encounter VS": '2.16.840.1.113762.1.4.1182.286'
valueset "Outpatient Encounter VS": '2.16.840.1.113883.3.464.1003.101.11.1277'

context Patient

define "Adult":
    AgeInYears() >= 18

define "Has Echo":
    exists(["Procedure": "Echo VS"])

define "Has HF Dx":
    exists(["Condition": "HF Dx VS"])

define "Inpatient Encounters With HF Dx":
    [Encounter: "Inpatient Encounter VS"] E
        with [Condition: "HF Dx VS"] C
            such that E.id = C.encounter

define "Outpatient Encounters With HF Dx":
    [Encounter: "Outpatient Encounter VS"] E
        with [Condition: "HF Dx VS"] C
            such that E.id = C.encounter

define "Encounter Criteria":
    exists("Inpatient Encounters With HF Dx") or
    Count("Outpatient Encounters With HF Dx") >= 2

define "Case":
    "Adult" and "Has Echo" and "Encounter Criteria"
