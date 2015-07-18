package me.declangao.wordpressreader.app;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import me.declangao.wordpressreader.R;
import me.declangao.wordpressreader.model.Post;

public class MainActivity extends AppCompatActivity implements
        RecyclerViewFragment.OnPostSelectedListener,
        PostFragment.OnCommentSelectedListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String TAB_LAYOUT_FRAGMENT_TAG = "TabLayoutFragment";
    public static final String POST_FRAGMENT_TAG = "PostFragment";
    public static final String COMMENT_FRAGMENT_TAG = "CommentFragment";

    private FragmentManager fm = null;
    private TabLayoutFragment tlf;
    private PostFragment pf;
    private CommentFragment cf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup action bar with the new toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fm = getSupportFragmentManager();

        // Setup fragments
        tlf = new TabLayoutFragment();
        pf = new PostFragment();
        cf = new CommentFragment();

        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.container, pf, POST_FRAGMENT_TAG);
        ft.add(R.id.container, cf, COMMENT_FRAGMENT_TAG);
        ft.add(R.id.container, tlf, TAB_LAYOUT_FRAGMENT_TAG);
        ft.hide(pf);
        ft.hide(cf);
        ft.commit();
    }

    /**
     * Invoked when a post in the list is selected
     *
     * @param post Selected Post object
     */
    @Override
    public void onPostSelected(Post post) {
        // Find the fragment in order to set it up later
        pf = (PostFragment) getSupportFragmentManager().findFragmentByTag(POST_FRAGMENT_TAG);

        // Set necessary arguments
        Bundle args = new Bundle();
        args.putInt("id", post.getId());
        args.putString("title", post.getTitle());
        args.putString("date", post.getDate());
        args.putString("author", post.getAuthor());
        args.putString("content", post.getContent());
        args.putString("url", post.getUrl());
        args.putString("thumbnailUrl", post.getThumbnailUrl());
        //pf.setArguments(args);
        // Configure PostFragment to display the right post
        pf.setUIArguments(args);

        // Show the fragment
        FragmentTransaction ft = fm.beginTransaction();
        ft.hide(tlf);
        ft.show(pf);
        ft.addToBackStack(null);
        ft.commit();
    }

    /**
     * Invoked when comment menu is selected
     *
     * @param id ID of the article, assigned by WordPress
     */
    @Override
    public void onCommentSelected(int id) {
        cf = (CommentFragment) getSupportFragmentManager().findFragmentByTag(COMMENT_FRAGMENT_TAG);
        Bundle args = new Bundle();
        args.putInt("id", id);
        //cf.setArguments(args);
        // Setup CommentFragment to display the right comments page
        cf.setUIArguments(args);

        FragmentTransaction ft = fm.beginTransaction();
        ft.hide(pf);
        ft.show(cf);
        ft.addToBackStack(null);
        ft.commit();
    }

    // Commented out coz we will let fragments handle their own Options Menus
    //@Override
    //public boolean onCreateOptionsMenu(Menu menu) {
    //    // Inflate the menu; this adds items to the action bar if it is present.
    //    getMenuInflater().inflate(R.menu.menu_main, menu);
    //    return true;
    //}

    //@Override
    //public boolean onOptionsItemSelected(MenuItem item) {
    //    // Handle action bar item clicks here. The action bar will
    //    // automatically handle clicks on the Home/Up button, so long
    //    // as you specify a parent activity in AndroidManifest.xml.
    //
    //    //int id = item.getItemId();
    //    //switch (id) {
    //    //    case R.id.action_refresh:
    //    //        plf.loadPosts();
    //    //        return true;
    //    //    case R.id.action_settings:
    //    //        return true;
    //    //    default:
    //    //        return super.onOptionsItemSelected(item);
    //    //}
    //
    //    return true;
    //}
}
