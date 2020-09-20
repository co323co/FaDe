package com.example.fade;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fade.DB.DBThread;
import com.example.fade.DB.entity.Person;

import java.util.ArrayList;

public class CustomDialog { }
interface CustomDialogClickListener {
    void onPositiveClick();
    void onNegativeClick();
}

class AddGroupDialog extends Dialog {

    Context context;
    private Button mPositiveButton;
    private Button mNegativeButton;

    MainActivity.rst result;

    private CustomDialogClickListener customDialogClickListener;

    //생성자 생성
    public AddGroupDialog(@NonNull Context context, MainActivity.rst result, CustomDialogClickListener customDialogClickListener) {
        super(context);
        this.context=context;
        this.customDialogClickListener=customDialogClickListener;
        this.result=result;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_add_group);

        //LeftListView에서 선택됐는지 안선택됐는지를 체크해놓기 위한 배열
        final ArrayList<Person> checkedList = new ArrayList<Person>();

        //셋팅
        mPositiveButton=(Button)findViewById(R.id.btn_addGroup_ok);
        mNegativeButton=(Button)findViewById(R.id.btn_addGroup_no);

        //호출하는 곳에서 인터페이스 설계해줌으로써 사용
        //클릭 리스너 셋팅 (클릭버튼이 동작하도록 만들어줌.)
        mPositiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                result.name = ((EditText)findViewById(R.id.et_add_groupName)).getText().toString();
                for(int i=0; i<checkedList.size(); i++) result.personIDList.add(checkedList.get(i).getPid());

                customDialogClickListener.onPositiveClick();
                dismiss();
            }
        });
        mNegativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customDialogClickListener.onNegativeClick();
                dismiss();
            }
        });

        final RecyclerView rv_right = findViewById(R.id.rv_addGroupPerson_right);
        ListView lv_left = findViewById(R.id.lv_addGroupPerson_left);

        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(getContext());
        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(getContext());
        rv_right.setLayoutManager(linearLayoutManager2);


        //어댑터 생성 후 리싸이클러뷰 어뎁터랑 연결
        final RightAdapter rightAdapter = new RightAdapter(checkedList);
        rv_right.setAdapter(rightAdapter);

        //왼쪽 스크롤 부분 and 클릭리스너 부분
        final int SELECTED_COLOR = Color.parseColor("#C3E8E4E4");
        final int DEFAULT_COLOR = Color.WHITE;

        final ArrayList<Person> personList=new ArrayList<Person>();

        DBThread.SelectPersonThraed t  = new DBThread.SelectPersonThraed(personList);
        t.start();
        try { t.join(); } catch (InterruptedException e) { e.printStackTrace(); }

        final boolean isChecked[] = new boolean[personList.size()];

        LeftListAdapter leftListAdapter = new LeftListAdapter(personList, isChecked);
        lv_left.setAdapter(leftListAdapter);

        //눌렀을 때 배경색 바뀌고 오른쪽으로 넘어가게끔
        lv_left.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                if (isChecked[i]==false){
                    view.setBackgroundColor(SELECTED_COLOR);
                    isChecked[i] = true;
                    checkedList.add(personList.get(i));
                    rightAdapter.notifyDataSetChanged();
                    rv_right.scrollToPosition(checkedList.size()-1);
                }
                else {
                    view.setBackgroundColor(DEFAULT_COLOR);
                    isChecked[i] = false;
                    checkedList.remove(personList.get(i));
                    rightAdapter.notifyDataSetChanged();
                }
            }
        });

    }

    class LeftListAdapter extends BaseAdapter{

        LayoutInflater mLayoutInflater = null;

        ArrayList<Person> personList;
        boolean isChecked[];

        int SELECTED_COLOR = Color.parseColor("#C3E8E4E4");

        LeftListAdapter(ArrayList<Person> personList, boolean isChecked[]){
            mLayoutInflater = LayoutInflater.from((context));
            this.personList=personList;
            this.isChecked = isChecked;
        }

        @Override
        public int getCount() { return personList.size(); }

        @Override
        public Object getItem(int i) { return personList.get(i); }

        @Override
        public long getItemId(int i) { return i; }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {

            view = mLayoutInflater.inflate(R.layout.item_profile_horizon, null);

            TextView name = view.findViewById(R.id.tv_profile_horizon_name);
            name.setText(personList.get(i).getName());

            ImageView profile = view.findViewById(R.id.iv_profile_horizon_profile);
            profile.setBackground(view.getContext().getDrawable(R.drawable.background_rounding));
            profile.setClipToOutline(true);

            ConvertFile convertFile = new ConvertFile(context);

            //프로필 사진 없으면 기본 이미지 띄움
            if(personList.get(i).getProfile_picture() != null){
                Bitmap bitmap = convertFile.byteArrayToBitmap(personList.get(i).getProfile_picture());
                profile.setImageBitmap(bitmap);
            }
            else
            { profile.setImageResource(R.drawable.ic_profile); }



            if (isChecked[i]==true) view.setBackgroundColor(SELECTED_COLOR);
            return view;
        }
    }

    class RightAdapter extends RecyclerView.Adapter<RightAdapter.RVHolder>{

        ArrayList<Person> checkedList;

        public class RVHolder extends RecyclerView.ViewHolder {
                View view;
                public RVHolder(@NonNull final View itemView) {
                    super(itemView);
                    this.view=itemView;
                }
        }
        //어뎁터 생성자
        RightAdapter(ArrayList<Person> checkedList) {
            this.checkedList=checkedList;
        }
        @NonNull
        @Override
        public RVHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_profile_horizon, parent, false);
            return new RVHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final RVHolder holder, final int position) {
            TextView tv_name = holder.view.findViewById(R.id.tv_profile_horizon_name);
            tv_name.setText(checkedList.get(position).getName());

            ImageView iv_profile = holder.view.findViewById(R.id.iv_profile_horizon_profile);
            iv_profile.setBackground(holder.view.getContext().getDrawable(R.drawable.background_rounding));
            iv_profile.setClipToOutline(true);

            ConvertFile convertFile = new ConvertFile(context);

            //프로필 사진 없으면 기본 이미지 띄움
            if(checkedList.get(position).getProfile_picture() != null){
                Bitmap bitmap = convertFile.byteArrayToBitmap(checkedList.get(position).getProfile_picture());
                iv_profile.setImageBitmap(bitmap);
            }
            else
            { iv_profile.setImageResource(R.drawable.ic_profile); }
        }

        @Override
        public int getItemCount() {
            return checkedList.size();
        }

    }
}

class EditGroupDialog extends Dialog {

    Context context;
    private Button mPositiveButton;
    private Button mNegativeButton;

    ArrayList<Integer> result;

    private CustomDialogClickListener customDialogClickListener;

    ArrayList<Person> checkedList = new ArrayList<Person>();
    ArrayList<Integer> checkedIDList;

    //생성자 생성
    public EditGroupDialog(@NonNull Context context, ArrayList<Integer> pidList, ArrayList<Integer> result, CustomDialogClickListener customDialogClickListener) {
        super(context);
        this.context=context;
        this.customDialogClickListener=customDialogClickListener;
        this.result=result;
        this.checkedIDList=pidList;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_add_group);
        findViewById(R.id.et_add_groupName).setVisibility(View.GONE);
        findViewById(R.id.top_line_of_dialogAddgroup).setVisibility(View.INVISIBLE);
        //LeftListView에서 선택됐는지 안선택됐는지를 체크해놓기 위한 배열

        //셋팅
        mPositiveButton=(Button)findViewById(R.id.btn_addGroup_ok);
        mNegativeButton=(Button)findViewById(R.id.btn_addGroup_no);

        //호출하는 곳에서 인터페이스 설계해줌으로써 사용
        //클릭 리스너 셋팅 (클릭버튼이 동작하도록 만들어줌.)
        mPositiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(int i=0; i<checkedList.size(); i++) result.add(checkedList.get(i).getPid());

                customDialogClickListener.onPositiveClick();
                dismiss();
            }
        });
        mNegativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customDialogClickListener.onNegativeClick();
                dismiss();
            }
        });

        final RecyclerView rv_right = findViewById(R.id.rv_addGroupPerson_right);
        ListView lv_left = findViewById(R.id.lv_addGroupPerson_left);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        rv_right.setLayoutManager(linearLayoutManager);


        //어댑터 생성 후 리싸이클러뷰 어뎁터랑 연결
        final RightAdapter rightAdapter = new RightAdapter(checkedList);
        rv_right.setAdapter(rightAdapter);

        //왼쪽 스크롤 부분 and 클릭리스너 부분
        final int SELECTED_COLOR = Color.parseColor("#C3E8E4E4");
        final int DEFAULT_COLOR = Color.WHITE;

        final ArrayList<Person> personList=new ArrayList<Person>();

        DBThread.SelectPersonThraed t  = new DBThread.SelectPersonThraed(personList);
        t.start();
        try { t.join(); } catch (InterruptedException e) { e.printStackTrace(); }

        final boolean isChecked[] = new boolean[personList.size()];

        //현재 그룹 구성원들은 체크함. pidList받아와서 비교하여 존재하면 check함
        for(int i =0; i<personList.size(); i++) {
            if(checkedIDList.contains(personList.get(i).getPid())) {
                isChecked[i] = true;
                checkedList.add(personList.get(i));
            }
        }

        LeftListAdapter leftListAdapter = new LeftListAdapter(personList, isChecked);
        lv_left.setAdapter(leftListAdapter);

        //눌렀을 때 배경색 바뀌고 오른쪽으로 넘어가게끔
        lv_left.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                if (isChecked[i]==false){
                    view.setBackgroundColor(SELECTED_COLOR);
                    isChecked[i] = true;
                    checkedList.add(personList.get(i));
                    rightAdapter.notifyDataSetChanged();
                    rv_right.scrollToPosition(checkedList.size()-1);
                }
                else {
                    view.setBackgroundColor(DEFAULT_COLOR);
                    isChecked[i] = false;
                    checkedList.remove(personList.get(i));
                    rightAdapter.notifyDataSetChanged();
                }
            }
        });

    }

    class LeftListAdapter extends BaseAdapter{

        LayoutInflater mLayoutInflater = null;

        ArrayList<Person> personList;
        boolean isChecked[];

        int SELECTED_COLOR = Color.parseColor("#C3E8E4E4");

        LeftListAdapter(ArrayList<Person> personList, boolean isChecked[]){
            mLayoutInflater = LayoutInflater.from((context));
            this.personList=personList;
            this.isChecked = isChecked;
        }

        @Override
        public int getCount() { return personList.size(); }

        @Override
        public Object getItem(int i) { return personList.get(i); }

        @Override
        public long getItemId(int i) { return i; }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {

            view = mLayoutInflater.inflate(R.layout.item_profile_horizon, null);

            TextView name = view.findViewById(R.id.tv_profile_horizon_name);
            name.setText(personList.get(i).getName());

            ImageView profile = view.findViewById(R.id.iv_profile_horizon_profile);
            profile.setBackground(view.getContext().getDrawable(R.drawable.background_rounding));
            profile.setClipToOutline(true);

            ConvertFile convertFile = new ConvertFile(context);

            //프로필 사진 없으면 기본 이미지 띄움
            if(personList.get(i).getProfile_picture() != null){
                Bitmap bitmap = convertFile.byteArrayToBitmap(personList.get(i).getProfile_picture());
                profile.setImageBitmap(bitmap);
            }
            else
            { profile.setImageResource(R.drawable.ic_profile); }


            if (isChecked[i]==true) view.setBackgroundColor(SELECTED_COLOR);
            return view;
        }
    }

    class RightAdapter extends RecyclerView.Adapter<RightAdapter.RVHolder>{

        ArrayList<Person> checkedList;

        public class RVHolder extends RecyclerView.ViewHolder {
            View view;
            public RVHolder(@NonNull final View itemView) {
                super(itemView);
                this.view=itemView;
            }
        }
        //어뎁터 생성자
        RightAdapter(ArrayList<Person> checkedList) {
            this.checkedList=checkedList;
        }
        @NonNull
        @Override
        public RVHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_profile_horizon, parent, false);
            return new RVHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final RVHolder holder, final int position) {
            TextView tv_name = holder.view.findViewById(R.id.tv_profile_horizon_name);
            tv_name.setText(checkedList.get(position).getName());

            ImageView iv_profile = holder.view.findViewById(R.id.iv_profile_horizon_profile);
            iv_profile.setBackground(holder.view.getContext().getDrawable(R.drawable.background_rounding));
            iv_profile.setClipToOutline(true);
            ConvertFile convertFile = new ConvertFile(context);

            //프로필 사진 없으면 기본 이미지 띄움
            if(checkedList.get(position).getProfile_picture() != null){
                Bitmap bitmap = convertFile.byteArrayToBitmap(checkedList.get(position).getProfile_picture());
                iv_profile.setImageBitmap(bitmap);
            }
            else
            { iv_profile.setImageResource(R.drawable.ic_profile); }
        }

        @Override
        public int getItemCount() {
            return checkedList.size();
        }

    }
}
