package kr.ac.jbnu.se.stkim.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Movie;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

import kr.ac.jbnu.se.stkim.R;
import kr.ac.jbnu.se.stkim.models.Book;
import kr.ac.jbnu.se.stkim.net.BookClient;

public class BookDetailActivity extends AppCompatActivity {

    private ImageView ivBookCover;
    private TextView tvTitle;
    private TextView tvAuthor;
    private TextView tvPublisher;
    private TextView tvPageCount;
    private BookClient client;
    //추가
    Button likeButton;
    Button unlikeButton;
    TextView likeCountView;
    TextView unlikeCountView;
    boolean likeState = false;
    boolean unlikeState = false;
    Book book;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);
        // Fetch views
        ivBookCover = (ImageView) findViewById(R.id.ivBookCover);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvAuthor = (TextView) findViewById(R.id.tvAuthor);
        tvPublisher = (TextView) findViewById(R.id.tvPublisher);
        tvPageCount = (TextView) findViewById(R.id.tvPageCount);

        likeCountView = (TextView) findViewById(R.id.likeCountView);
        unlikeCountView = (TextView) findViewById(R.id.unlikeCountView);
        likeButton = findViewById(R.id.likeButton);
        unlikeButton = findViewById(R.id.unlikeButton);

        likeCountView.setText("0");
        unlikeCountView.setText("0");

        // Use the book to populate the data into our views

        book = (Book) getIntent().getSerializableExtra(BookListActivity.BOOK_DETAIL_KEY);
        loadBook(book);


        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!unlikeState) {
                    if (likeState) {
                        decrLikeCount();
                    } else {
                        incrLikeCount();
                    }
                    likeState = !likeState;


                    FirebaseDatabase.getInstance()
                            .getReference("Book")
                            .child(book.getOpenLibraryId())
                            .setValue(book);

                    updateView();
//                    db.collection("book")
//                            .document("가나")
//                            .update("likeCount", likeCount,
//                                    "like", likeState)
//                            .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                @Override
//                                public void onSuccess(Void aVoid) {
//
//                                }
//                            })
//                            .addOnFailureListener(new OnFailureListener() {
//                                @Override
//                                public void onFailure(@NonNull Exception e) {
//                                    Log.w("ERROR", "Error updating document", e);
//                                }
//                            });
                }
            }
        });

        unlikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!likeState) {
                    if (unlikeState) {
                        decrUnLikeCount();
                    } else {
                        incrUnLikeCount();
                    }
                    unlikeState = !unlikeState;

                    FirebaseDatabase.getInstance()
                            .getReference("Book")
                            .child(book.getOpenLibraryId())
                            .setValue(book);

                    updateView();

//                    db.collection("book")
//                            .document("평가")
//                            .update("unlikeCount", unlikeCount,
//                                    "unlike", unlikeState)
//                            .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                @Override
//                                public void onSuccess(Void aVoid) {
//
//                                }
//                            })
//                            .addOnFailureListener(new OnFailureListener() {
//                                @Override
//                                public void onFailure(@NonNull Exception e) {
//                                    Log.w("ERROR", "Error updating document", e);
//                                }
//                            });
                }
            }
        });


        updateView();
    }

    // Populate data for the book
    private void loadBook(Book book) {
        //change activity title
        this.setTitle(book.getTitle());
        // Populate data
        Picasso.get().load(Uri.parse(book.getCoverUrl())).error(R.drawable.ic_nocover).into(ivBookCover);
        tvTitle.setText(book.getTitle());
        tvAuthor.setText(book.getAuthor());

        // fetch extra book data from books API
        client = new BookClient();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_book_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_share) {
            setShareIntent();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setShareIntent() {
        ImageView ivImage = (ImageView) findViewById(R.id.ivBookCover);
        final TextView tvTitle = (TextView) findViewById(R.id.tvTitle);
        // Get access to the URI for the bitmap
        Uri bmpUri = getLocalBitmapUri(ivImage);
        // Construct a ShareIntent with link to image
        Intent shareIntent = new Intent();
        // Construct a ShareIntent with link to image
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("*/*");
        shareIntent.putExtra(Intent.EXTRA_TEXT, (String) tvTitle.getText());
        shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
        // Launch share menu
        startActivity(Intent.createChooser(shareIntent, "Share Image"));

    }

    // Returns the URI path to the Bitmap displayed in cover imageview
    public Uri getLocalBitmapUri(ImageView imageView) {
        // Extract Bitmap from ImageView drawable
        Drawable drawable = imageView.getDrawable();
        Bitmap bmp = null;
        if (drawable instanceof BitmapDrawable) {
            bmp = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        } else {
            return null;
        }
        // Store image to default external storage directory
        Uri bmpUri = null;
        try {
            File file = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS), "share_image_" + System.currentTimeMillis() + ".png");
            file.getParentFile().mkdirs();
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            bmpUri = Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }

    public void incrLikeCount() {
        likeCountView.setText("1");
        likeButton.setBackgroundResource(R.drawable.ic_thumb_up_selected);
        book.setLike(true);
    }

    public void decrLikeCount() {
        
        likeCountView.setText("0");
        likeButton.setBackgroundResource(R.drawable.ic_thumb_up);
        book.setLike(false);
    }

    public void incrUnLikeCount() {
        unlikeCountView.setText("1");
        unlikeButton.setBackgroundResource(R.drawable.ic_thumb_down_selected);
        book.setUnlike(true);
    }

    public void decrUnLikeCount() {
        unlikeCountView.setText("0");
        unlikeButton.setBackgroundResource(R.drawable.ic_thumb_down);
        book.setUnlike(false);
    }

    public void updateView() {

        FirebaseDatabase.getInstance()
                .getReference("Book")
                .child(book.getOpenLibraryId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Book loadedBook = dataSnapshot.getValue(Book.class);
                        Log.d("DDDDDDDDDDDDDDDD", book.toString());

                        if(loadedBook != null){
                            likeState = loadedBook.isLike();
                            unlikeState = loadedBook.isUnlike();

                            if (likeState) {
                                likeButton.setBackgroundResource(R.drawable.ic_thumb_up_selected);
                                likeCountView.setText("1");
                            }

                            if (unlikeState) {
                                unlikeButton.setBackgroundResource(R.drawable.ic_thumb_down_selected);
                                unlikeCountView.setText("1");
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

//        db.collection("book")
//                .document("평가")
//                .get()
//                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                    @Override
//                    public void onSuccess(DocumentSnapshot documentSnapshot) {
//                        Book book = documentSnapshot.toObject(Book.class);
//                        likeState = book.isLike();
//                        unlikeState = book.xsxsxsxsxxsxsxsxsxsxssx
//
// isUnlike();
//
//                        if (likeState) {
//                            likeButton.setBackgroundResource(R.drawable.ic_thumb_up_selected);
//                        }
//
//                        if (unlikeState) {
//                            unlikeButton.setBackgroundResource(R.drawable.ic_thumb_down_selected);
//                        }
//
//                        likeCountView.setText(String.valueOf(likeCount));
//                        unlikeCountView.setText(String.valueOf(unlikeCount));
//                    }
//                });
    }

}

