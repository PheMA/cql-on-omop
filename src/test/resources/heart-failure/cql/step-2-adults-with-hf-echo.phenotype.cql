library "PhEMA Heart Failure" version '0.2.0'

using QUICK

valueset "Echo VS": '2.16.840.1.999999.1'

define "Adult":
	AgeInYears() >= 18

define "Has Echo":
	exists(["Procedure": "Echo VS"])

define "Case":
	"Adult" and "Has Echo"
