library "PhEMA Heart Failure" version '0.2.0'

using QUICK

valueset "Echo VS": '2.16.840.1.999999.1'
valueset "HF Dx VS": '2.16.840.1.113883.3.526.3.376'

context Patient

define "Adult":
    AgeInYears() >= 18

define "Has Echo":
    exists(["Procedure": "Echo VS"])

define "Has HF Dx":
    exists(["Condition": "HF Dx VS"])

define "Case":
    "Adult" and "Has Echo" and "Has HF Dx"
