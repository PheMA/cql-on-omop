/**
 * Test Phenotype
 *
 * Name : Test Phenotype
 * ID   : 0
 * Url  : https://phekb.org/node/0
 */
library "0.test-phenotype" version '1.0.0'

using FHIR version '4.0.0'

include FHIRHelpers version '4.0.0' called FHIRHelpers

valueset "Atrial Fibrillation Heart Transplant Codes": '0.atrial-fibrillation-heart-transplant-codes'
valueset "Atrial Fibrillation Condition Codes": '0.atrial-fibrillation-condition-codes'
valueset "Atrial Fibrillation Procedure Codes": '0.atrial-fibrillation-proc-codes'

context Patient

define "Had Heart Transplant":
    exists([Procedure: "Atrial Fibrillation Heart Transplant Codes"])

define "Cardiac Procedure":
    exists([Procedure: "Atrial Fibrillation Procedure Codes"])

define "Cardiac Surgery-Related Condition":
    exists([Condition: "Atrial Fibrillation Condition Codes"])

define "Cardiac Surgery":
    "Cardiac Procedure" or "Cardiac Surgery-Related Condition"

define "Case":
    "Cardiac Surgery"
        and
    (not "Had Heart Transplant")