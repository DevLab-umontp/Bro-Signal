package me.remi.espie.brosignal;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.HashMap;

public class ViewPagerAdapter extends FragmentStateAdapter {

    private ArrayList<Fragment> list = new ArrayList<>();

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
            return list.get(position);
    }

    public void addFragment(int position, Fragment fragment){
        list.add(position, fragment);
        notifyItemChanged(position);
    }

    public void addFragment(Fragment fragment){
        list.add(fragment);
        notifyItemChanged(list.size());
    }

    public void removeFragment(int position){
        list.remove(position);
        notifyItemChanged(position);
    }

    public void refreshFragment(int position, Fragment fragment){
        list.remove(position);
        list.add(position, fragment);
        notifyItemChanged(position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

}
