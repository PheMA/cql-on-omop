using QUICK

valueset "Diabetes": '2.16.840.1.113883.3.464.1003.103.12.1001'

parameter "Measurement Period" Interval<DateTime>

context Patient 

define "In Initial Population":
  ["Condition": "Diabetes"] C
