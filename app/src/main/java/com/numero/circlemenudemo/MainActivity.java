package com.numero.circlemenudemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.numero.circlemenu.CircleMenuLayout;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int itemImages[] = new int[] {
                R.mipmap.ic_launcher,
                R.mipmap.ic_launcher,
                R.mipmap.ic_launcher,
                R.mipmap.ic_launcher,
                R.mipmap.ic_launcher,
                R.mipmap.ic_launcher
        };

        String itemTexts[] = new String[] {
                "1",
                "2",
                "3",
                "4",
                "5",
                "6"
        };

        CircleMenuLayout circleMenuLayout = (CircleMenuLayout) findViewById(R.id.circle_menu);
        circleMenuLayout.setMenuItemIconsAndTexts(itemImages, itemTexts);
        circleMenuLayout.setOnMenuItemClickListener(new CircleMenuLayout.OnMenuItemClickListener() {
            @Override
            public void itemClick(View view, int position) {
                Toast.makeText(MainActivity.this, "touched " + position, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
