library "PhEMA Heart Failure" version '0.2.0'

using QUICK

context Patient

valueset "Echo VS": '2.16.840.1.999999.1'
valueset "HF Dx VS": '2.16.840.1.999999.2'

define "Adult":
    AgeInYears() >= 18

define "Has Echo":
    exists(["Procedure": "Echo VS"])

define "Has HF Dx":
    exists(["Condition": "HF Dx VS"])

define "Case":
    "Adult" and "Has Echo" and "Has HF Dx"
