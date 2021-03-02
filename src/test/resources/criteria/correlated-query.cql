library "Correlated Query Test"

using QUICK

//include PhEMAHelpers version '1.0.0' called PhEMA

valueset "Diabetes VS": '1.2.3.4.5'
valueset "Inpatient VS": '6.7.8.9.10'
valueset "Outpatient VS": '11.12.13.14.15'

valueset "Some Dx 1": 'some-dx-value-set-1'
valueset "Some Dx 2": 'some-dx-value-set-2'
valueset "Some Proc 1": 'some-dx-value-set-3' // mismatched value set - does not matter for testing
valueset "Some Proc 2": 'some-proc-value-set-2'
valueset "Some Rx 1": 'some-rx-value-set-1'
valueset "Some Labs 1": 'some-lab-value-set-1'

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

/**
  Correlations for Phenotype Workbench demo
 */
define "Workbench Demo Correlation Simple":
    exists(
        [Condition: "Some Dx 1"] C
            where
                exists(
                    [Observation: "Some Labs 1"] O
                        where O.appliesDateTime in Interval[C.onsetDateTime - 1 day, C.onsetDateTime + 1 day]
                )
   )

define "Workbench Demo Correlation Multiple":
    exists(
        [Condition: "Some Dx 1"] C
            where
                exists(
                    [Observation: "Some Labs 1"] O
                        // "appliesDateTime" seems to be an old QUICK thing... hopefully switching to FHIR will help
                        where O.appliesDateTime in Interval[C.onsetDateTime - 1 day, C.onsetDateTime + 1 day]
                )
                    and
                exists(
                    [Procedure: "Some Proc 1"] P
                        where P.performedDateTime in Interval[C.onsetDateTime - 1 day, C.onsetDateTime + 1 day]
                    )
                    and
                 exists(
                    [Condition: "Some Dx 2"] D
                        where D.onsetDateTime in Interval[C.onsetDateTime - 1 day, C.onsetDateTime + 1 day]
                )
    )

define "Workbench Demo Correlation Multiple Nested":
    exists(
        [Condition: "Some Dx 1"] C
            where
                exists(
                    [Observation: "Some Labs 1"] O
                    // "appliesDateTime" seems to be an old QUICK thing... hopefully switching to FHIR will help
                        where O.appliesDateTime in Interval[C.onsetDateTime - 1 day, C.onsetDateTime + 1 day]
                )
                    and
                exists(
                    [Procedure: "Some Proc 1"] P
                        where P.performedDateTime in Interval[C.onsetDateTime - 1 day, C.onsetDateTime + 1 day]
                )
                    and
                (
                    exists(
                        [Condition: "Some Dx 2"] D
                            where D.onsetDateTime in Interval[C.onsetDateTime - 1 day, C.onsetDateTime + 1 day]
                    )
                        or
                    exists(
                        [Condition: "Diabetes VS"] E
                            where E.onsetDateTime in Interval[C.onsetDateTime - 1 day, C.onsetDateTime + 1 day]
                    )
                )
                    and
                exists(
                    [Procedure: "Some Proc 2"] F
                        where F.performedDateTime in Interval[C.onsetDateTime - 1 day, C.onsetDateTime + 1 day]
                )
           )