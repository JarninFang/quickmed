#CURRENT TO DO LIST

##Survey Handling
- [ ] Need to adequately handle when different fragments are loaded, save the current fragment's responses
to the XML, and re-parse.

- [ ] Properly change the question types to better reflect what kind of question is being asked
	FreeResponse
	YesNo
	ValueRange[5/10] <- Not sure what android display asset this is
	NumericalAnswer

- [ ] Change the app's displayed assets to reflect the question type correctly:
	FreeResponse = EditText
	YesNo = Yes or No selection
	ValueRange = ?
	NumericalAnswer = value.RestrictDigits

- [ ] Copy the master question file to a temp file which will be the current patient's responses. This temp
file will be pushed to the DB/excel (already formatted correctly, thanksfully) and then destroyed for
the new patient to enter their responses.