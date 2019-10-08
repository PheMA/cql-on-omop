library "Diagnosis-Count" version '1'

using QUICK

valueset "Diabetes VS": '1.2.3.4.5'
valueset "Hypertension VS": '2.3.4.5.6'
valueset "Afib VS": '3.4.5.6.7'

context Patient

define "Diabetes with Count":
  Count([Condition: "Diabetes VS"]) > 2

// Expressions used to comprise other expressions - not directly tested
define "Diabetes Expression":
  [Condition: "Diabetes VS"]

define "Hypertension Expression":
  [Condition: "Hypertension VS"]

define "Afib Expression":
  [Condition: "Afib VS"]

define "Afib exists":
  exists("Afib Expression")


// Expressions to exercise different integration tests

define "Exists direct condition":
  exists([Condition: "Diabetes VS"])

define "Exists from expression":
  exists("Diabetes Expression")

define "Diabetes exists":
  exists("Diabetes Expression")

define "Hypertension exists":
  exists("Hypertension Expression")

define "Exists from referenced expression":
  "Exists from expression"

define "Or direct conditions":
  exists([Condition: "Diabetes VS"])
  or exists([Condition: "Hypertension VS"])

define "Or from expressions":
  exists("Diabetes Expression")
  or exists("Hypertension Expression")

define "And from expressions":
  exists("Diabetes Expression")
  and exists("Hypertension Expression")

define "Or mixed direct and expression":
  exists("Diabetes Expression")
  or exists([Condition: "Hypertension VS"])

define "Or from referenced expressions":
  "Diabetes exists"
  or "Hypertension exists"

define "Nested boolean direct conditions":
  exists([Condition: "Diabetes VS"])
  and (exists([Condition: "Hypertension VS"]) or exists([Condition: "Afib VS"]))

define "Two nested boolean direct conditions":
  (exists([Condition: "Diabetes VS"]) and exists([Condition: "Afib VS"]))
  or (exists([Condition: "Hypertension VS"]) or exists([Condition: "Afib VS"]))

define "Nested boolean from expressions":
  "Diabetes exists"
  and ("Hypertension exists" or "Afib exists")

define "Nested boolean from referenced boolean expressions":
  "Or from expressions"
  or "And from expressions"

define "Greater than direct condition":
  Count([Condition: "Diabetes VS"]) > 2

define "Greater than or equal direct condition":
  Count([Condition: "Diabetes VS"]) >= 2

define "Equal direct condition":
  Count([Condition: "Diabetes VS"]) = 2

define "Less than direct condition":
  Count([Condition: "Diabetes VS"]) < 2

define "Less than or equal direct condition":
  Count([Condition: "Diabetes VS"]) <= 2

// This is not really supported - it returns a Null expression when evaluated.  It makes a good test to explicitly
// document that it is not supported.
define "Count from expression":
  count("Diabetes Expression") > 2

define "Count expression reference":
  "Diabetes with Count"

define "Age":
  AgeInYears() between 40 and 75

define "Temporal relationship":
  [Condition: "Diabetes VS"] DM
  with [Condition: "Hypertension VS"] HTN
    such that HTN.onsetDateTime 30 days or less before DM.onsetDateTime

define "Temporal relationship - flipped criteria":
  [Condition: "Diabetes VS"] DM
  with [Condition: "Hypertension VS"] HTN
    such that DN.onsetDateTime 30 days or less before HTN.onsetDateTime

define "Temporal relationship static interval":
  [Condition: "Diabetes VS"] C where C.onsetDateTime during Interval[@2018-01-01, @2019-01-01)
