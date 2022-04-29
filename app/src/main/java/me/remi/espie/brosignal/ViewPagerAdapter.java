package me.remi.espie.brosignal;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;

/**
 * Adapter pour les fragments
 */
public class ViewPagerAdapter extends FragmentStateAdapter {

    /**
     * Liste des fragments instanciés
     */
    private final ArrayList<Fragment> list = new ArrayList<>();

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
            return list.get(position);
    }

    /**
     * Ajoute un @fragment à la position @position
     * @param position position du fragment à ajouter
     * @param fragment fragment à ajouter
     */
    public void addFragment(int position, Fragment fragment){
        list.add(position, fragment);
        notifyItemChanged(position);
    }

    /**
     * Ajoute un fragment au bout de la liste
     * @param fragment fragment à ajouter
     */
    public void addFragment(Fragment fragment){
        list.add(fragment);
        notifyItemChanged(list.size());
    }

    /**
     * Supprime le fragment à la position @position
     * @param position position du fragment à supprimer
     */
    public void removeFragment(int position){
        list.remove(position);
        notifyItemRemoved(position-1);
    }

    /**
     * Supprime le fragment @fragment de la liste
     * @param fragment fragment à supprimer
     */
    public void removeFragment(Fragment fragment){
        int pos = list.indexOf(fragment);
        if (pos>-1) removeFragment(pos);
    }

    /**
     * Remplace le fragment à la position @position par @fragment
     * @param position position du fragment à rafraichir
     * @param fragment nouveau fragment à instancier
     */
    public void refreshFragment(int position, Fragment fragment){
        list.remove(position);
        list.add(position, fragment);
        notifyItemChanged(position);
    }

    /**
     * Retourne le @fragment à la position @position
     * @param position position du fragment à retourner
     * @return fragment à la position @position
     */
    public Fragment getFragment(int position){
        return list.get(position);
    }

    /**
     * Rafraichis tous les fragments aux positions >1
     * Soit les fragments des groupes de BROs
     */
    public void refreshBrolists(){
        for (int i =1 ; i<list.size(); i++) notifyItemChanged(i);
    }

    /**
     * Rafraichis tous les fragments
     */
    public void refreshAll(){
        for (int i = 0; i < list.size(); i++) {
            notifyItemChanged(i);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

}
