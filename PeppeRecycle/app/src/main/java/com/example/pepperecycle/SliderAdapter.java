package com.example.pepperecycle;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

public class SliderAdapter extends PagerAdapter {

    Context context;
    LayoutInflater layoutInflater;

    public SliderAdapter(Context context) {
        this.context = context;
    }

    //Arrays
    public String[] slide_titles = {
            "Come si gioca:",
            "Il tuo turno",
            "Il mio turno"
            //TODO https://youtu.be/byLKoPgB7yA?t=506
    };

    public String[] slide_tViewTutorial = {
            "\nA turno, il giudice ci mostrerà un oggetto \n"
                    + "e dovremo indovinare in quale bidone riciclarlo.\n"
                    + "Una volta scelto il tipo di bidone, \n"
                    + "il giudice dovrà dire se la risposta è corretta o no.\n"
                    + "Se sì, si guadagnerà un punto!\n",

            "Qui, sul mio tablet, ti mostrerò quattro bidoni:\n"
                    + "organico, plastica e metalli, carta e cartone, vetro.\n"
                    + "Tu dovrai scegliere il bidone dove buttare il rifiuto mostrato.\n"
                    + "Come si fa? È semplice!\n"
                    + "Basta che tu mi dica la tipologia di rifiuto (ad esempio, plastica)\n"
                    + "oppure il colore del bidone, o, ancora, il numero (ad esempio, secondo bidone).\n"
                    + "Una volta che avrai scelto il bidone,\n"
                    + "il tuo turno sarà finito \n"
                    + "e toccherà al giudice stabilire se la risposta è corretta o no!\n",

            "Quando toccherà a me, vi chiederò di mostrarmi l’oggetto\n" +
                    "e aspetterò una vostra risposta prima di dirvi dove riciclarlo!\n"
                    + "Dopo che avrò scelto il bidone,\n"
                    + "il mio turno sarà finito \n"
                    + "e toccherà al giudice stabilire se la risposta è corretta o no!\n"
            //TODO https://youtu.be/byLKoPgB7yA?t=506
    };

    @Override
    public int getCount() {
        return slide_titles.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == (RelativeLayout) o;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.slide_layout, container, false);

        //https://youtu.be/byLKoPgB7yA?t=671
        TextView titleTutorial = (TextView) view.findViewById(R.id.titleTutorial);
        TextView tViewTutorial = (TextView) view.findViewById(R.id.tViewTutorial);
        titleTutorial.setText(slide_titles[position]);
        tViewTutorial.setText(slide_tViewTutorial[position]);

        container.addView(view);

        return view;

    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((RelativeLayout) object);
    }

}
