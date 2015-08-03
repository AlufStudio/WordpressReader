package me.declangao.wordpressreader.app;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ImageView;

import com.android.volley.Response;
import com.android.volley.toolbox.ImageRequest;

import me.declangao.wordpressreader.R;

/**
 * Fragment to display content of a post in a WebView.
 * Activities that contain this fragment must implement the
 * {@link PostFragment.PostListener} interface
 * to handle interaction events.
 */
public class PostFragment extends Fragment {
    private static final String TAG = "PostFragment";

    private int id;
    private String title;
    private String content;
    private String url;
    private String thumbnailUrl;

    private WebView webView;

    private ImageView featuredImageView;
    private Toolbar toolbar;
    private NestedScrollView nestedScrollView;

    private PostListener mListener;

    public PostFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        // Needed to show Options Menu
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_post, container, false);

        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        //((MainActivity)getActivity()).setSupportActionBar(toolbar);

        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout)
                rootView.findViewById(R.id.collapsingToolbarLayout);
        collapsingToolbarLayout.setTitle(getString(R.string.app_name));

        nestedScrollView = (NestedScrollView) rootView.findViewById(R.id.nestedScrollView);

        featuredImageView = (ImageView) rootView.findViewById(R.id.featuredImage);

        // Create the WebView
        webView = (WebView) rootView.findViewById(R.id.webview_post);

        Log.d(TAG, "onCreateView()");

        return rootView;
    }

    /**
     * Since we can't call setArguments() on an existing fragment, we make our own!
     *
     * @param args Bundle containing information about the new post
     */
    public void setUIArguments(final Bundle args) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                // Clear the content first
                webView.loadData("", "text/html; charset=UTF-8", null);
                featuredImageView.setImageBitmap(null);

                id = args.getInt("id");
                title = args.getString("title");
                String date = args.getString("date");
                String author = args.getString("author");
                content = args.getString("content");
                url = args.getString("url");
                thumbnailUrl = args.getString("thumbnailUrl");

                // Download featured image
                final String featuredImageUrl = args.getString("featuredImage");
                ImageRequest ir = new ImageRequest(featuredImageUrl, new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        featuredImageView.setImageBitmap(bitmap);
                    }
                }, 0, 0, null, null);
                AppController.getInstance().addToRequestQueue(ir);

                // Construct HTML content
                // First, some CSS
                String html = "<style>img{max-width:100%;height:auto;} " +
                        "iframe{width:100%;}</style> ";
                // Article Title
                html += "<h2>" + title + "</h2> ";
                // Date & author
                html += "<h4>" + date + " " + author + "</h4>";
                // The actual content
                html += content;

                // Enable JavaScript in order to be able to Play Youtube videos
                webView.getSettings().setJavaScriptEnabled(true);
                webView.setWebChromeClient(new WebChromeClient());

                // Load and display HTML content
                // Use "charset=UTF-8" to support non-English language
                webView.loadData(html, "text/html; charset=UTF-8", null);

                Log.d(TAG, "Showing post, ID: " + id);
                Log.d(TAG, "Featured Image: " + featuredImageUrl);

                // Reset Actionbar
                ((MainActivity) getActivity()).setSupportActionBar(toolbar);
                ((MainActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

                // Make sure the article starts from the very top
                // Delayed coz it can take some time for WebView to load HTML content
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        nestedScrollView.smoothScrollTo(0, 0);
                    }
                }, 500);
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.d(TAG, "onCreateOptionsMenu()");

        inflater.inflate(R.menu.menu_post, menu);

        // Get share menu item
        MenuItem item = menu.findItem(R.id.action_share);
        // Initialise ShareActionProvider
        // Use MenuItemCompat.getActionProvider(item) since we are using AppCompat support library
        ShareActionProvider shareActionProvider =
                (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        // Share the article URL
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT, title + "\n" + url);
        shareActionProvider.setShareIntent(i);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_comments:
                mListener.onCommentSelected(id);
                return true;
            case R.id.action_share:
                return true;
            case R.id.action_send_to_wear:
                sendToWear();
                return true;
            case android.R.id.home:
                mListener.onHomePressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Send a BigTextStyle notification with text contents of the post to Android Wear devices
     */
    private void sendToWear() {
        // Intent used to run app on the phone from watch
        Intent viewIntent = new Intent(getActivity(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getActivity(), 0, viewIntent, 0);

        // Use BigTextStyle to read long notification
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        //bigTextStyle.setBigContentTitle(title);
        // Use Html.fromHtml() to remove HTML tags
        bigTextStyle.bigText(Html.fromHtml(content));

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity());
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentIntent(pendingIntent)
                .setStyle(bigTextStyle);

        // Load thumbnail as background image
        ImageRequest ir = new ImageRequest(thumbnailUrl, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap bitmap) {
                builder.setLargeIcon(bitmap);

                NotificationManagerCompat notificationManagerCompat =
                        NotificationManagerCompat.from(getActivity());
                notificationManagerCompat.cancel(id);
                notificationManagerCompat.notify(id, builder.build());
            }
        }, 0, 0, null, null);

        AppController.getInstance().getRequestQueue().add(ir);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (PostListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement PostListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface PostListener {
        void onCommentSelected(int id);
        void onHomePressed();
    }

}
