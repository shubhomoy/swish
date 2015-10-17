package com.bitslate.swish.SwishFragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bitslate.swish.PlanListActivity;
import com.bitslate.swish.R;
import com.bitslate.swish.SwishObjects.User;
import com.bitslate.swish.SwishUtilities.Config;
import com.bitslate.swish.SwishUtilities.SwishGCM;
import com.bitslate.swish.SwishUtilities.SwishPreferences;
import com.bitslate.swish.SwishUtilities.SwishRequest;
import com.bitslate.swish.SwishUtilities.VolleySingleton;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by shubhomoy on 13/10/15.
 */
public class SignupFragment extends Fragment {

    CallbackManager callbackManager;
    LoginButton authButton;
    ProfileTracker mProfileTracker;
    String emailString;
    SwishPreferences prefs;
    ProgressDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_signup, container, false);
        authButton = (LoginButton) v.findViewById(R.id.login_button);
        authButton.setFragment(this);
        authButton.setReadPermissions(Arrays.asList("user_friends", "email"));
        authButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                mProfileTracker = new ProfileTracker() {
                    @Override
                    protected void onCurrentProfileChanged(Profile profile, Profile profile2) {
                        Log.v("facebook - profile", profile2.toString());
                        mProfileTracker.stopTracking();
                    }
                };
                mProfileTracker.startTracking();
                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject jsonObject, GraphResponse graphResponse) {
                                JSONObject object = graphResponse.getJSONObject();
                                try {
                                    emailString = object.getString("email");
                                } catch (JSONException e) {
                                    Log.i("option", "Error");
                                }
                                if(emailString!=null) {
                                    Log.d("option", emailString);
                                    signin(emailString);
                                }
                            }
                        }
                );
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,first_name,last_name, email");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {
                Log.d("option", error.toString());
            }
        });
        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getActivity());
        callbackManager = CallbackManager.Factory.create();
        prefs = new SwishPreferences(getActivity());
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Signing in");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void signin(final String emailString) {
        progressDialog.show();
        String url = Config.SWISH_URL+"/signup";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String accessToken = jsonObject.getString("accessToken");
                    prefs.setAccessToken(accessToken);
                    Gson gson = new Gson();
                    User user = gson.fromJson(jsonObject.getString("data"), User.class);
                    prefs.setUser(user);

                    SwishGCM swishGCM = new SwishGCM(getActivity());
                    new RegisterInBg().execute();

                } catch (JSONException e) {
                    Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("option", error.toString());
                progressDialog.dismiss();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("id", Profile.getCurrentProfile().getId());
                params.put("email", emailString);
                params.put("fname", Profile.getCurrentProfile().getFirstName());
                params.put("lname", Profile.getCurrentProfile().getLastName());
                params.put("access_token", AccessToken.getCurrentAccessToken().getToken());
                return params;
            }
        };
        VolleySingleton.getInstance().getRequestQueue().add(stringRequest);
    }

    public class RegisterInBg extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setTitle("Registering");
            progressDialog.setMessage("Please wait");
        }

        @Override
        protected String doInBackground(Void... voids) {
            String msg = "";

            try {
                if (SwishGCM.gcm == null) {
                    SwishGCM.gcm = GoogleCloudMessaging.getInstance(getActivity());
                }
                SwishGCM.regid = SwishGCM.gcm.register(SwishGCM.SENDER_ID);
                msg = "1";

                // You should send the registration ID to your server over HTTP,
                // so it can use GCM/HTTP or CCS to send messages to your app.
                // The request to your server should be authenticated if your app
                // is using accounts.
                //Log.d("option","REGISTERATION ID IS : "+PinpostGCM.regid);

                // For this demo: we don't need to send it because the device
                // will send upstream messages to a server that echo back the
                // message using the 'from' address in the message.

                // Persist the registration ID - no need to register again.
                SharedPreferences prefs = getActivity().getSharedPreferences("Swish", Context.MODE_PRIVATE);
                int appVersion = SwishGCM.getAppVersion(getActivity());
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(SwishGCM.PROPERTY_REG_ID, SwishGCM.regid);
                editor.putInt("app_version", appVersion);
                editor.commit();
            } catch (IOException ex) {
                msg = "0";
                // If there is an error, don't just keep trying to register.
                // Require the user to click a button again, or perform
                // exponential back-off.
            }
            return msg;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s.isEmpty()) {
                if (progressDialog.isShowing()) progressDialog.dismiss();
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Slow connection");
                builder.setCancelable(false);
                builder.setMessage("Registration was incomplete. Try again?");
                builder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new RegisterInBg().execute();
                    }
                });
                builder.create().show();
            } else if (s.equals("1")) {
                completeRegistration();
            }
        }
    }

    void completeRegistration() {
        String url = Config.SWISH_API_URL+"/insertGcm";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                startActivity(new Intent(getActivity(), PlanListActivity.class));
                getActivity().finish();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("option", error.toString());
                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());
                builder.setTitle("Retry?");
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        completeRegistration();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.create().show();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("accessToken", prefs.getAccessToken());
                headers.put("id", prefs.getUser().fb_id);
                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("user_id", String.valueOf(prefs.getUser().id));
                params.put("gcm_id", prefs.getGcmId());
                return params;
            }
        };
        VolleySingleton.getInstance().getRequestQueue().add(stringRequest);
    }

}
