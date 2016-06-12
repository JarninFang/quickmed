package com.example.jarnin.quickmed;

import android.content.Context;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.StringReader;
import org.xmlpull.v1.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.*;


/* Things to know about implementing a parser:
    each tag opening is going to be called an event,
    we're going to extract the data from certain events on event.START_TAG and
    cut off data saving for this tag on event.END_TAG. at least so long until
    event != parser.END_DOCUMENT

    NOTE: In order to instantiate a new parser, we need to also instantiate
    a XMLFactoryObject class:

private XmlPullParserFactory xmlFactoryObject = XmlPullParserFactory.newInstance();
private XmlPullParser myparser = xmlFactoryObject.newPullParser();

myparser.setInput("file", null);

example:
int event = myParser.getEventType();
while (event != XmlPullParser.END_DOCUMENT)
{
   String name=myParser.getName();
   switch (event){
      case XmlPullParser.START_TAG:
      break;

      case XmlPullParser.END_TAG:
      if(name.equals("temperature")){
         temperature = myParser.getAttributeValue(null,"value");
      }
      break;
   }
   event = myParser.next();
}


 */

public class QuestionXmlParser {
    private String survey = "survey";
    private String section = "section";
    private String title = "title";
    private String question = "question";
    private String text = "text";
    private String type = "type";
    private String response = "response";

    private static ArrayList<Question> questionArray = new ArrayList<Question>();
    private static ArrayList<Section> sectionArray = new ArrayList<Section>();
    private int xmlResourceFile = 0;
    private XmlPullParserFactory xmlFactoryObject;
    public volatile boolean parsingComplete = true;
    private Context context;

    public QuestionXmlParser(Context context) {
        this.context = context;
    }

    public String getTitle() {
        return this.title;
    }

    public String getQuestion() {
        return this.question;
    }

    public String getText() {
        return this.text;
    }

    public String getType() {
        return this.type;
    }

    public String getResponse() {
        return this.response;
    }

    public static class Section {
        private String section = "";
        private int questionNumber;
        private int numQuestionsThisSection;
        private ArrayList<Question> thisSectionsQuestions;

        public Section (String section) {
            this.section = section;
            thisSectionsQuestions = new ArrayList<Question>();
            this.questionNumber = 0;
        }

        private void addQuestionToThisSection(Question question) {
            thisSectionsQuestions.add(question);
            //Log.e("SECTION", "added a new question to section: " + this.section);
        }

        public Question getNextQuestionThisSection() {
            //Log.e("NUMBER", "current question number: " + this.questionNumber);
            Question returnQuestion = thisSectionsQuestions.get(this.questionNumber);
            this.questionNumber++;
            if(this.questionNumber >= thisSectionsQuestions.size())
                questionNumber = 0;
            return returnQuestion;

        }

        public int getNumQuestionsThisSection() {
            return this.thisSectionsQuestions.size();
        }

        //this method is intended to serve the section name upward given the arg from the drawer
        // handling the fragments
        public String getSectionName() {
            return section;
        }

        public synchronized void setResponse(int questionNumber, String response) {
            //Log.e("RESPONSE", "to question #: " + questionNumber + " setting response: " +
	        //       response);
            thisSectionsQuestions.get(questionNumber).setResponseText(response);
        }

	    public Question getSpecificQuestion(int qNumber) {
		    return thisSectionsQuestions.get(qNumber);
	    }
    }
    public class Question {
        private String section = "";
        private String questionText = "";
        private String questionType = "";
        private String response = "";

        public Question (String section, String questionType, String
                         questionText, String response) {
            this.section = section;
            this.questionType = questionType;
            this.questionText = questionText;
            this.response = response;
        }

        public String getSectionName () {
            return this.section;
        }

        public String getQuestionText() {
            return this.questionText;
        }

        public String getQuestionType() {
            return this.questionType;
        }
        public String getResponse() {
            return this.response;
        }

        public void setResponseText(String response) {
            this.response = response;
        }
    }

    public Question getNextQuestion(int questionSection, int questionNumber) {
        return sectionArray.get(questionSection).getNextQuestionThisSection();
    }

    public int getNumQuestionsThisSection(int section) {
        return sectionArray.get(section).getNumQuestionsThisSection();
    }


    private void pushNewQuestionToList(Question newQuestion, int sectionNumber) {
        this.questionArray.add(newQuestion);
        sectionArray.get(sectionNumber).addQuestionToThisSection(newQuestion);
    }

    /*
        We need a method to save this section's responses to the XML based on what fragment we're
         leaving. This requires we get ARG_SECTION_NUMBER from the activity so we parse only that
          section. Apparently though it will parse linearly through the xml file but maybe that
          doesn't matter..
          questions for another time.
     */
    public void saveFragmentDataToTempSurvey(View rootView, int sectionID) {
        /*
            need to iterate through all the elements in this view and store the values to the
            questions_patient temp xml file
        */

        LinearLayout lay = (LinearLayout) rootView.findViewById(R.id.fragment_linear_questions);
        //now it would be easy to just take total children / 2 and iterate every second child
        // (question/answer question/answer etc) but some of these questions will have more than
        // one element in total of that particular question. Not sure yet how to find which
        // element is a child or not...
        ArrayList<View> touchables = lay.getTouchables();

        //Log.e("TOUCHABLES", "CURRENT SIZE OF sectionArray: " + sectionArray.size());
        //Log.e("TOUCHABLES", "CURRENT SIZE OF touchables: " + touchables.size());

        for(int i = 0; i < sectionArray.get(sectionID).getNumQuestionsThisSection(); i++) {//if
	        // (touchables.get(1).getTag() ==
	        // "qtag1"){
	        String qTag = "qtag" + i;
	        EditText t = (EditText) lay.findViewWithTag(qTag);
	        //Log.e("TOUCHABLES", "ID of t: " + t.get);
	        //if (t != null) {
		        String response = t.getText().toString();
		        //Log.e("TOUCHABLES", t.getText().toString());
		        this.sectionArray.get(sectionID).setResponse(i, response);
	        //} else
		      //  Log.e("TOUCHABLES", "get text is null");
	        //sectionArray.get(0).setResponse(0, t
	        // .getText().toString());
	        //}
        }


    }

	/*Since we now have the data persisting through fragment transitions, we need to store the
	data into the patient xml file.

	The R.raw.questions_patient.xml file is suppoed to be a temporary internal storage saved
	file, deleted when patient data is pushed to the db server.
	 */
	public void pushAllDataToPatientXML() {
		final String xmlFile = "questions_patient.xml";

		int event;
		String entryText = null;
		InputStream stream = context.getResources().openRawResource(R.raw.questions_patient);


		//Log.e("SAVING", "saving to this dir: " + context.getApplicationContext().getFilesDir()
		//		.getAbsolutePath());
		try {
			xmlFactoryObject = XmlPullParserFactory.newInstance();
			XmlPullParser parser = xmlFactoryObject.newPullParser();
			parser.setInput(stream, null);
			event = parser.getEventType();
			int sectionNumber = 0;
			int questionNumber = 0;

			//FileOutputStream fileos = new FileOutputStream(new File(context.getFilesDir() +
			//		xmlFile), false);

			FileOutputStream fileos = new FileOutputStream(new File(context.getFilesDir()
					+ "/" + xmlFile));
			Log.e("SAVING", "Saving as file: " + context.getFilesDir()+"/"+xmlFile);
			//FileOutputStream fileos =
			// context
			// .openFileOutput(xmlFile, Context
			// .MODE_PRIVATE);
			XmlSerializer xmlSerializer = Xml.newSerializer();
			StringWriter writer = new StringWriter();
			xmlSerializer.setOutput(writer);
			xmlSerializer.startDocument("UTF-8", false);
			String lastEntryName = null;
			while(event != XmlPullParser.END_DOCUMENT) {
				String entryName = parser.getName();

				switch (event) {
					case XmlPullParser.START_TAG:
						xmlSerializer.startTag(null, entryName);
						lastEntryName = entryName;
						break;
					case XmlPullParser.TEXT:
						entryText = parser.getText();
						if(lastEntryName.equals("response")) { //ERROR HAPPENING HERE
							String res = sectionArray.get(sectionNumber)
									.getSpecificQuestion(questionNumber).getResponse();

							xmlSerializer.text(res);// +
									//"</response>");
							Log.e("RESPONSE", "Setting response to this question: " + res);
							questionNumber++;
							xmlSerializer.endTag(null, "response");
						}
						else
							xmlSerializer.text(entryText);
						break;
					case XmlPullParser.END_TAG:
						//if(lastEntryName.equals("response"))
						//	xmlSerializer.endTag(null, lastEntryName);
						//else
						if(!entryName.equals("response"))
							xmlSerializer.endTag(null, entryName);
						lastEntryName = "none response";
						if(entryName.equals("section")){
							sectionNumber++;
							questionNumber = 0;
						}
						break;
				}

				event = parser.next();
			}
			xmlSerializer.endDocument();
			xmlSerializer.flush();

			String dataWrite = writer.toString();
			fileos.write(dataWrite.getBytes());
			fileos.close();

			Log.e("SAVING", "end of writing to file");
		}catch(Exception e) {
				e.printStackTrace();
		}

	}

    public void parseXMLAndStoreIt() {
        int event;
        String entryText = null;
        InputStream stream = context.getResources().openRawResource(R.raw.questions_master);

        try {
            xmlFactoryObject = XmlPullParserFactory.newInstance();
            XmlPullParser parser = xmlFactoryObject.newPullParser();
            parser.setInput(stream, null);
            event = parser.getEventType();
            int sectionNumber = 0;
            while(event != XmlPullParser.END_DOCUMENT) {
                String entryName = parser.getName();
                //Log.e("parser", "parseXMLAndStoreIt: " + entryName);
                //Log.e("parser", "event: " + event);
                switch(event) {
                    case XmlPullParser.START_TAG:
                        break;

                    case XmlPullParser.TEXT:
                        entryText = parser.getText();
                        break;
                    case XmlPullParser.END_TAG:
                        //section title, won't change until we hit new section
                        if (entryName.equals("title")) {
                            title = entryText;
                            sectionArray.add(new Section(title));
                            Log.e("SECTION", "added new section: " + title);
                            sectionNumber++;
                        }

                        //at this point we should have all info needed to make
                        //a question: section Title, question text, type, response
                        else if (entryName.equals("question")){
                            Question questionToPush = new Question(this.title, this.type, this
                                    .text, this.response);
                            pushNewQuestionToList(questionToPush, sectionNumber-1);

                        }

                        else if (entryName.equals("text"))
                            text = entryText;
                        else if (entryName.equals("type"))
                            type = entryText;
                        else if (entryName.equals("response"))
                            response = entryText;
                        else {
                            //something screwed up severly here. if we're here
                        }
                        break;
                }

                event = parser.next();
            }

            parsingComplete = false;
        }

        catch(Exception e) {
            e.printStackTrace();
        }

        parsingComplete = true;
    }
}
