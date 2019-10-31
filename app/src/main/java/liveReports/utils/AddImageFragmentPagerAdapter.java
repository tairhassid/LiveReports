package liveReports.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class AddImageFragmentPagerAdapter extends FragmentPagerAdapter {

    private final List<Fragment> fragmentList;
    private final List<String> fragmentNamesList;

    public AddImageFragmentPagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
        fragmentList = new ArrayList<>();
        fragmentNamesList = new ArrayList<>();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return super.getPageTitle(position);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }

    public void addFragment(Fragment fragment, String name) {
        fragmentList.add(fragment);
        fragmentNamesList.add(name);
    }

}
