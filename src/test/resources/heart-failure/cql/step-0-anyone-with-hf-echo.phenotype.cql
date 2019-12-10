library "PhEMA Heart Failure" version '0.0.0'

using QUICK

valueset "Echo VS": '2.16.840.1.999999.1'

context Patient

define "Case":
	exists(["Procedure": "Echo VS"])
