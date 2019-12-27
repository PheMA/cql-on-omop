library "Correlated Query Test"

using QUICK

valueset "Diabetes VS": '1.2.3.4.5'
valueset "Inpatient VS": '6.7.8.9.10'
valueset "Outpatient VS": '11.12.13.14.15'

context Patient

define "Adult":
    AgeInYears() >= 18

define "Simplest Case":
    [Encounter: "Inpatient VS"] E
       with [Condition: "Diabetes VS"] C
          such that E.id = C.encounter

define "Diabetes Outpatient Encounter":
    [Encounter: "Outpatient VS"] E
       with [Condition: "Diabetes VS"] C
          such that E.id = C.encounter

/* This is currently not supported */
define "Multiple Correlations":
     [Encounter: "Inpatient VS"] E
       with [Condition: "Diabetes VS"] C
          such that E.id = C.encounter and C.clinicalStatus = 'active'

define "With Aggregate":
    Count("Simplest Case") > 3

define "Simple Aggregate":
    Count([Condition: "Diabetes VS"]) >= 55

define "Encounter Criteria":
    exists("Diabetes Outpatient Encounter") or
    Count("Simplest Case") >= 2

define "Adult Diabetics":
    "Adult" and exists([Condition: "Diabetes VS"])