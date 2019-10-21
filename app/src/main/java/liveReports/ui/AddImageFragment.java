package liveReports.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import liveReports.livereports.R;
import liveReports.utils.AddImageFragmentPagerAdapter;


public class AddImageFragment extends Fragment {

    private AddImageFragmentPagerAdapter adapter;
    private ViewPager viewPager;
    private View rootView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_add_image, container, false);
        setupViewPager();
        return rootView;
    }

    private void setupViewPager() {
        adapter = new AddImageFragmentPagerAdapter(
                getChildFragmentManager(),
                FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
//                behavior- only current fragment is resumed
        adapter.addFragment(new GalleryFragment(), "gallery");
        adapter.addFragment(new PhotoFragment(), "photo");
        //TODO: constants for the names

        viewPager = rootView.findViewById(R.id.photo_container);
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = rootView.findViewById(R.id.add_image_tabLayout);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.getTabAt(0).setText("Gallery");
        tabLayout.getTabAt(1).setText("Photo");
        //TODO add to strings file and getString(R.string. ...)


    }

}
