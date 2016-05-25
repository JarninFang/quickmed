package com.example.jarnin.quickmed;

import android.content.res.Configuration;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;
import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;

public class SurveyFormActivity extends AppCompatActivity {
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] mSections;

    private static QuestionXmlParser parser;
    /*
        for the first time we create the SurveyFormActivity (at least for
        now, this might be entirely wrong later) we're going to parse through
         the question xml and hold the questions to be called by
         ARG_SECTION_NUMBER in the drawer menu. Since we're going to start
         with populating the topmost menu item, i.e the General Information,
         then we'll populate those questions first

     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTitle = mDrawerTitle = getTitle();
        mSections = getResources().getStringArray(R.array.sections_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mSections));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_menu);
        getSupportActionBar().setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_action_menu,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        /*
            this seems like a good place to parse the XML file.
         */

        //QuestionXmlParser parser = new QuestionXmlParser("questions.xml");
        parser = new QuestionXmlParser(this.getApplicationContext());
        //XmlPullParser pullParser = Xml.newPullParser();
        //parser.parseXMLAndStoreIt(pullParser);
        //XmlResourceParser xmlParser = this.getResources().getXml(R.xml
        //       .questions);
        parser.parseXMLAndStoreIt();
        int questionNumber = 0;

        if (savedInstanceState == null) {
            //select the top most item in the drawer (in this
            // case the general information fragment
            selectItem(0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_blank, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        //menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
        //MenuItem item= menu.findItem(R.id.action_settings);
        //item.setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action buttons
        switch(item.getItemId()) {
            /*case R.id.action_websearch:
                // create intent to perform web search for this planet
                Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                intent.putExtra(SearchManager.QUERY, getSupportActionBar().getTitle());
                // catch event that there's no activity to handle intent
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Toast.makeText(this, R.string.app_not_available, Toast.LENGTH_LONG).show();
                }
                return true;
            */
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /* The click listener for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
        // update the main content by replacing fragments
        SurveyFormFragment fragment = new SurveyFormFragment();
        Bundle args = new Bundle();
        args.putInt(SurveyFormFragment.ARG_SECTION_NUMBER, position);
        fragment.setArguments(args);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(mSections[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * Fragment that appears in the "content_frame", shows a planet
     */
    public static class SurveyFormFragment extends Fragment {
        public static final String ARG_SECTION_NUMBER = "section_number";

        public SurveyFormFragment() {
            // Empty constructor required for fragment subclasses
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.survey_question_form, container,
                    false);
            int i = getArguments().getInt(ARG_SECTION_NUMBER);
            String section = getResources().getStringArray(R.array.sections_array)[i];

            /*TextView tv_question1= (TextView) rootView.findViewById(R.id
                    .surveyQuestion_tv_box1);
            TextView tv_question2= (TextView) rootView.findViewById(R.id
                    .surveyQuestion_tv_box2);
            EditText et_question1 = (EditText) rootView.findViewById(R.id
                    .surveyQuestion_et_box1);
            */

            /*
                Right now we can set the text for the fragment, but we're
                going to need a way to populate the text given:
                 what section we're currently in (by getInt
                 (ARG_SECTION_NUMBER) most likely)
                 which questions have yet to be answered.
                The second problem we're going to likely tackle later. First
                let's work on just getting all the damn questions adapted and
                 listed outright... which means we're going to need to
                 dynamically add to the layout xml to add more
                 TextView/EditText assets to the fragment.

                 Note that the fragment is gotten above by rootView
             */
            int questionNumbersToDisplay = 10;
            QuestionXmlParser.Question q;

            LinearLayout lay = (LinearLayout) rootView.findViewById(R.id.fragment_linear_questions);
            TextView tv_question = new TextView(getActivity());
            EditText et_response = new EditText(getActivity());
            ArrayList<TextView> arrayQuestionTexts= new ArrayList<TextView>();
            ArrayList<EditText> arrayResponseTexts= new ArrayList<EditText>();
            int variableQNum = questionNumbersToDisplay;
            int thisQuestionNumber = 0;
            while(variableQNum > 0) {
                q = parser.getNextQuestion(section, thisQuestionNumber);

                arrayQuestionTexts.add(new TextView(getActivity()));
                arrayQuestionTexts.get(arrayQuestionTexts.size()-1).setText(q.getQuestionText());
                arrayResponseTexts.add(new EditText((getActivity())));
                //arrayResponseTexts.get(arrayResponseTexts.size()-1);
                variableQNum--;
                thisQuestionNumber++;
            }
            //populate the fragment
            while(!arrayQuestionTexts.isEmpty()) {
                lay.addView(arrayQuestionTexts.get(0));
                lay.addView(arrayResponseTexts.get(0));

                arrayQuestionTexts.remove(0);
                arrayResponseTexts.remove(0);

            }


            //this is bad coding. Wanted to just get some questions up and running, but now we're
            // going to make it extendable for all questions in a particular section

            /*QuestionXmlParser.Question q2 = parser.getNextQuestion(section,
                    questionNumbersToDisplay);
            QuestionXmlParser.Question q3 = parser.getNextQuestion(section,
                    questionNumbersToDisplay);
            QuestionXmlParser.Question q4 = parser.getNextQuestion(section,
                    questionNumbersToDisplay);
            QuestionXmlParser.Question q5 = parser.getNextQuestion(section,
                    questionNumbersToDisplay);
            */
            /*tv_question1.setText(q1.getQuestionText());
            tv_question2.setText(q2.getQuestionText());
            */


            getActivity().setTitle(section);
            return rootView;
        }
    }
}