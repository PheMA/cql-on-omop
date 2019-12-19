library "Correlated Query Test"

using QUICK

valueset "Diabetes VS": '1.2.3.4.5'
valueset "Inpatient VS": '6.7.8.9.10'

define "Simplest Case":
    [Encounter: "Inpatient VS"] E
       with [Condition: "Diabetes VS"] C
          such that E.id = C.encounter

define "Multiple Correlations":
     [Encounter: "Inpatient VS"] E
       with [Condition: "Diabetes VS"] C
          such that E.id = C.encounter and C.clinicalStatus = 'active'