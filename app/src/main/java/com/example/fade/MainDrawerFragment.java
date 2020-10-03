package com.example.fade;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fade.DB.DBThread;
import com.example.fade.DB.entity.Person;
import com.example.fade.Server.CommServer;
import com.example.fade.Server.PersonData;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

public class MainDrawerFragment extends Fragment {

    final int CODE_REGI_PERSON = 0;
    PersonAdapter personAdapter;
    ArrayList<PersonData> personList=new ArrayList<PersonData>();
    RecyclerView rv;
    int n=0;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_drawer_main,container,false);

        //리싸이클러뷰 만들고 설정
        rv  = view.findViewById(R.id.rv_nameList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(view.getContext());
        rv.setLayoutManager(linearLayoutManager);
        rv.addItemDecoration(new DividerItemDecoration(view.getContext(),1));

        //personList에 DB불러오기
        //new DBThread.SelectPersonThraed(personList).start();
        //어댑터 생성 후 리싸이클러뷰 어뎁터랑 연결

        Handler handler = new Handler();

        new Thread(){
            @Override
            public void run() {
                //personList.addAll(new CommServer(view.getContext()).getAllPersons());
                personList = new CommServer(view.getContext()).getAllPersons();

                handler.post(() -> {
                    //어댑터 생성 후 리싸이클러뷰 어뎁터랑 연결
                    personAdapter = new PersonAdapter(personList, MainDrawerFragment.this);
                    rv.setAdapter(personAdapter);
                });
            }
        }.start();



        Button addButton = view.findViewById(R.id.btn_addPerson);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), RegiPersonActivity1.class);
                startActivity(intent);
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        personList.clear();
        rv.removeAllViewsInLayout();

        Handler handler = new Handler();
        new Thread(){
            @Override
            public void run() {
                personList.addAll(new CommServer(getView().getContext()).getAllPersons());
                handler.post(() -> {
                    //어댑터 생성 후 리싸이클러뷰 어뎁터랑 연결
                    personAdapter = new PersonAdapter(personList, MainDrawerFragment.this);
                    rv.setAdapter(personAdapter);
                });
            }
        }.start();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //인물등록하고 넘어옴
        if(requestCode==CODE_REGI_PERSON){
            if (requestCode==RESULT_OK) Log.d("argtest", "Result OK");
            else {
                Log.d("argtest", "Result CANCLE");
            }
            if(data==null)
            {
                Log.d("argtest", "main : data is null");
                return;
            }
            String profile_name = data.getExtras().getString("profile_name");
            byte[] profile_thumbnail = data.getExtras().getByteArray("profile_thumbnail");

        }
    }
}

//PersonRecyclerViewAdapter
class  PersonAdapter extends RecyclerView.Adapter<PersonAdapter.PVHolder>{

    ArrayList<PersonData> personList;
    Context context;
    MainDrawerFragment mainDrawerFragment;
    public PersonAdapter(Context context){
        this.context = context;
    }



    class PVHolder extends  RecyclerView.ViewHolder {

        public View view;
        public PVHolder(@NonNull View itemView) {
            super(itemView);
            view=itemView;
        }
    }

    PersonAdapter(ArrayList<PersonData> personList, MainDrawerFragment mainDrawerFragment){
        this.personList=personList;
        this.mainDrawerFragment=mainDrawerFragment;
    }



    @NonNull
    @Override
    public PVHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_namelist, parent, false);
        return new PVHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final PVHolder holder, final int position) {

        TextView tv_name = holder.view.findViewById(R.id.tv_nameList_name);
        tv_name.setText(personList.get(position).getName());


        ImageView iv_profile = holder.view.findViewById(R.id.iv_nameList_profile);
        iv_profile.setBackground(holder.view.getContext().getDrawable(R.drawable.background_rounding));
        iv_profile.setClipToOutline(true);

        ConvertFile convertFile = new ConvertFile(context);

        //프로필 사진 없으면 기본 이미지 띄움
        if(personList.get(position).getThumbnail() != null){
            Bitmap bitmap = convertFile.byteArrayToBitmap(personList.get(position).getThumbnail());
            iv_profile.setImageBitmap(bitmap);
        }
        else
        { iv_profile.setImageResource(R.drawable.ic_profile); }

        Button btn_subPerson = holder.view.findViewById(R.id.btn_subPerson);
        btn_subPerson.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Handler handler = new Handler();
                Thread t = new Thread(){
                    @Override
                    public void run() {
                        //서버에서 person 지워줌
                        new CommServer(holder.view.getContext()).deletePerson(LoginActivity.UserEmail, personList.get(position).getId());
                        //TODO: 인물이 삭제되면 연관된 그룹 모델 재학습하고 GROUP_INFO(CSV파일 대신하는 테이블)수정해야함!!

                        handler.post(() -> {
                            //인물리스트가 바뀌었으니  그룹리스트뷰도 새로고침 해준다.
                            ((MainActivity)MainActivity.CONTEXT).onResume();
                            mainDrawerFragment.onResume();
                            Toast.makeText(holder.view.getContext(),"사람삭제를 성공했습니다!", Toast.LENGTH_SHORT).show();
                        });
                    }
                }; t.start();
            }
        });
    }

    @Override
    public int getItemCount() {
        return personList.size();
    }

}