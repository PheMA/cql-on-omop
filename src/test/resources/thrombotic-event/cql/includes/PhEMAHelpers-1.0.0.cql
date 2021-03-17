/**
    Common functions and statements used in PhEMA phenotypes

	NOTE: This is a bit of a mess at the moment, sorry, hopefully we can evole
		  a more consistent API as we figure out what's needed
*/
library PhEMAHelpers version '1.0.0'

using FHIR version '4.0.0'

include FHIRHelpers version '4.0.0' called FHIRHelpers
include PhEMATemporal version '1.0.0' called PhEMATemporal

codesystem "LOINC": 'http://loinc.org'

valueset "Biobank Specimens Available": '2.16.840.1.113762.1.4.1026.1'

code "Yes": 'LA33-6' from "LOINC" display 'Yes'

// We will use this concept to act as the default concept if no explicit
// value is provided for the biobank status value.
concept "Default Biobank Sample Exists": { "Yes" } display 'Default Biobank Sample Exists'

context Patient

define function "Earliest" (
	conditions List<FHIR.Condition>
):
	First(
		(conditions) C
			sort by start of PhEMATemporal."Normalize Onset"(onset)
	)

define function "Earliest" (
	observations List<FHIR.Observation>
):
	First(
		(observations) O
			sort by start of PhEMATemporal."Normalize Interval"(effective)
	)

define function "Earliest" (
	medications List<FHIR.MedicationRequest>
):
	First(
		(medications) C
			sort by start of PhEMATemporal."Normalize Onset"(authoredOn)
	)

define function "Effective Date" (
	obs FHIR.Observation
):
	start of PhEMATemporal."Normalize Onset"(obs.effective)


define function "Effective Date" (
	dx FHIR.Condition
):
	start of PhEMATemporal."Normalize Interval"(dx.onset)

define function "Effective Date" (
	rx FHIR.MedicationRequest
):
	start of PhEMATemporal."Normalize Interval"(rx.authoredOn)

define function "Effective Date" (
	px FHIR.Procedure
):
	start of PhEMATemporal."Normalize Interval"(px.performed)

define function "Earliest Date" (
	dates List<System.DateTime>
):
	First(
		(
            (dates) D
                where D is not null
                return {
                    IndexDate: D
                }
        ) DD
        sort by IndexDate
    ).IndexDate

define function "Latest Date" (
	dates List<System.DateTime>
):
	First(
		(
            (dates) D
                where D is not null
                return {
                    IndexDate: D
                }
        ) DD
        sort by IndexDate desc
    ).IndexDate

define function "Latest" (
	observations List<FHIR.Observation>
):
	First(
		(observations) O
			sort by start of PhEMATemporal."Normalize Interval"(effective) desc
	)

define function "Sorted Dates"(
	list List<MedicationRequest> // TODO: include other resource types
):
	(
		("Effective Dates"(list)) D
			where D is not null
			return {
				IndexDate: D
			}
	) DD
	sort by IndexDate

define function "Number Of Observations Before Date" (
	observations List<FHIR.Observation>,
	endDate System.DateTime
):
	if endDate is null then
		Count(observations)
	else
		Count(
			( observations ) O
				where start of PhEMATemporal."Normalize Interval"( O.effective ) before endDate
		)

define function "Number Of Encounters Before Date" (
	encounters List<FHIR.Encounter>,
	endDate System.DateTime
):
	if endDate is null then
		Count(encounters)
	else
		Count(
			( encounters ) E
				where start of PhEMATemporal."Normalize Interval"( E.period.start ) before endDate
		)

define function "Dx Count" (
	dx System.Concept
):
	Count([Condition: dx])

///// Function Overloading Below

define function "Effective Dates" (
	obs List<FHIR.Observation>
):
	(obs) O
		return "Effective Date"(O)

define function "Effective Dates" (
	dx List<FHIR.Condition>
):
	(dx) D
		return "Effective Date"(D)

define function "Effective Dates" (
	rx List<FHIR.MedicationRequest>
):
	(rx) R
		return "Effective Date"(R)

define function "Effective Dates" (
	px List<FHIR.Procedure>
):
	(px) P
		return "Effective Date"(P)

///// Common eMERGE Phenotype Functions

define "In Biobank":
  // We are assuming that the presence of a biobank specimen result with no value means that
  // the sample exists.
  exists([Observation: "Biobank Specimens Available"] O
    where Coalesce(O.value, "Default Biobank Sample Exists") ~ "Yes")


define "Is Male":
	Patient.gender ~ 'male'

define "Is Female":
	Patient.gender ~ 'female'


///// Medication exposure functions

/**
	Converts a time-based quantitiy to days with a granularity of 1. If the
	source < 1, it will be rounded up to 1.

	For example, if we are converting 2.0 'hours' to 'days', the result will be 1.0
*/
define function "Convert To Days With Granularity"(
	source System.Quantity
):
	if (convert source to 'days').value < 1.0 then
		System.Quantity { value: 1.0, unit: 'days' }
	else
		convert source to 'days'

/**
	Get the exposure in days. If the dosage instructions do not specify any
	repitition, assume one day of exposure.
*/
define function "Get Exposure Days"(
	request MedicationRequest
):
	// FIXME: 🚨 This only looks at the first dosage instruction
	if request.dosageInstruction[0].timing.repeat.bounds is null then
		System.Quantity { value: 1.0, unit: 'days' }
	else
		// FIXME: 🚨 This only looks at the first dosage instruction, and
		// 			 and assumes the type is Duration
		"Convert To Days With Granularity"(
			FHIRHelpers.ToQuantity(First(request.dosageInstruction.timing.repeat.bounds) as FHIR.Duration)
		)

/**
	Get the sum total of exposure in days for a list of MedicationRequests
*/
define function "Get Total Exposure Days"(
	requests List<MedicationRequest>
):
	Sum(
		(requests R return "Get Exposure Days"(R))
	)


define function "Earliest Exposure Date"(
	requests List<MedicationRequest>
):
	// This assumes the medication was start on the date that the request was authored
	"Earliest Date"(
		(requests R return R.authoredOn)
	)

/**
	If the dosage instructions contain a repeat instruction, calculated the end
	date of the repeating period
*/
define function "Latest Exposure Date"(
	requests List<MedicationRequest>
):
	"Latest Date"(
		(
			requests R
				return if R.dosageInstruction.timing.repeat is null then
					R.authoredOn
				else
					// FIXME: 🚨 This only looks at the first dosage instruction, and
					// 			 and assumes the type is Duration
					R.authoredOn
						+
					FHIRHelpers.ToQuantity(First(R.dosageInstruction.timing.repeat.bounds) as FHIR.Duration)
		)
	)
