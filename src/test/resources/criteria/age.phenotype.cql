library "Age Criteria Test"

using QUICK

valueset "Diabetes VS": '1.2.3.4.5'

context Patient

// Simple operator tests

define "Age gte Value":
    AgeInYears() >= 18

define "Value gte Age":
    18 >= AgeInYears()

define "Age eq Value":
    AgeInYears() = 33

define "Value eq Age":
    33 = AgeInYears()

define "Age neq Value":
    AgeInYears() != 44

define "Value neq Age":
    44 != AgeInYears()

define "Age lt Value":
    AgeInYears() < 55

define "Value lt Age":
    55 < AgeInYears()

define "Age lte Value":
    AgeInYears() <= 66

define "Value lte Age":
    66 <= AgeInYears()

define "Age gt Value":
    AgeInYears() > 77

define "Value gt Age":
    77 > AgeInYears()

// Error cases

define "Error bad precision":
    AgeInWeeks() < 99

define "Error non-numeric":
    AgeInYears() = 'string'

// Nested tests

define "Nested Ages 1":
    AgeInYears() < 18 or AgeInYears() >= 81

define "Nested Ages 2":
    AgeInYears() >= 18 and AgeInYears() < 65

define "Nested Ages 3":
    AgeInYears() < 18 or (AgeInYears() >= 65 and AgeInYears() < 87)

define "Diabetes and Specific Ages":
    exists(["Condition": "Diabetes VS"]) and "Nested Ages 3"