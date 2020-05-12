package com.tencent.shadow.sample.host;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tencent.shadow.dynamic.host.EnterCallback;
import com.tencent.shadow.sample.constant.Constant;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HostFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class HostFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HostFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HostFragment newInstance(String param1, String param2) {
        HostFragment fragment = new HostFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        startPlugin();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_host, container, false);
    }

    public void startPlugin() {

        PluginHelper.getInstance().singlePool.execute(new Runnable() {
            @Override
            public void run() {
                HostApplication.getApp().loadPluginManager(PluginHelper.getInstance().pluginManagerFile);

                Bundle bundle = new Bundle();
                bundle.putString(Constant.KEY_PLUGIN_ZIP_PATH, PluginHelper.getInstance().pluginZipFile.getAbsolutePath());
                bundle.putString(Constant.KEY_PLUGIN_PART_KEY, "sample-plugin-app");
                bundle.putString(Constant.KEY_ACTIVITY_CLASSNAME, "com.tencent.shadow.sample.plugin.app.lib.gallery.splash.SplashActivity");

                // 注意，这个enter是标准的代理模式，代理的是插件的PluginManagerImpl
                HostApplication.getApp().getPluginManager().enter(getActivity(), Constant.FROM_ID_START_ACTIVITY, bundle, null);
            }
        });
    }
}
