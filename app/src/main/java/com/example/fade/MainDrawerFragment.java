package com.example.fade;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
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
import com.example.fade.DB.entity.Group;
import com.example.fade.DB.entity.Person;
import com.example.fade.Server.CommServer;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

public class MainDrawerFragment extends Fragment {

    final int CODE_REGI_PERSON = 0;
    PersonAdapter personAdapter;
    ArrayList<Person> personList=new ArrayList<Person>();
    ArrayList<Group> groupList = new ArrayList<Group>();
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
        new DBThread.SelectPersonThraed(personList).start();
        //어댑터 생성 후 리싸이클러뷰 어뎁터랑 연결
        personAdapter = new PersonAdapter(personList);
        rv.setAdapter(personAdapter);

        Button addButton = view.findViewById(R.id.btn_addPerson);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getContext(), RegiPersonActivity1.class);
                startActivity(intent);
//                getActivity().finish();
//                n++;
//                DBThread.InsertPersonThraed t1 = new DBThread.InsertPersonThraed(person);
//                //꼭 삽입하고 리스트뷰 갱신을 위해 personList를 바뀐 DB로 재갱신 해줘야함!
//                DBThread.SelectPersonThraed t2 = new DBThread.SelectPersonThraed(personList);
//                t1.start();
//                //join은 스레드가 끝날 때까지 기다려 줌
//                try { t1.join(); } catch (InterruptedException e) { e.printStackTrace(); }
//                t2.start();
//                try { t2.join(); } catch (InterruptedException e) { e.printStackTrace(); }
//                personAdapter.notifyDataSetChanged();
//                //포커스를 맨 아래로 맞춰줌
//                rv.scrollToPosition(personList.size()-1);
//                //메인엑티비티의 그룹리스트뷰도 새로고침해줌. 그룹에 사람 추가할 때, 방금 추가한 사람은 보기에 안뜰까 봐임
//               ((MainActivity)MainActivity.CONTEXT).onResume();
            }
        });
        return view;
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

    ArrayList<Person> personList;
    Context context;

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

    PersonAdapter(ArrayList<Person> personList){
        this.personList=personList;
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
        if(personList.get(position).getProfile_picture() != null){
            Bitmap bitmap = convertFile.byteArrayToBitmap(personList.get(position).getProfile_picture());
            iv_profile.setImageBitmap(bitmap);
        }
        else
        { iv_profile.setImageResource(R.drawable.ic_profile); }



        Button btn_subPerson = holder.view.findViewById(R.id.btn_subPerson);
        btn_subPerson.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                /////////////////만약 삭제될 pid를 가지고있는 그룹이 있다면 그룹 내에서 pid를 지워주는 코드
                int pid = personList.get(position).getPid();
                //삭제될 pid를 가지고 있는 그룹들을 찾아냄
                ArrayList<Integer> gidList = new ArrayList<Integer>();
                DBThread.SelectGidListByPid t = new DBThread.SelectGidListByPid(pid, gidList);
                t.start();
                try { t.join(); } catch (InterruptedException e) { e.printStackTrace(); }
                Log.e("gid리스트 사이즈", gidList.size()+"");
                //각 그룹의 personIDList에서 삭제될 pid를 지움
                if (gidList.size()==0){
                    Thread thread = new Thread(){
                        @Override
                        public void run() {
                            new CommServer(holder.view.getContext()).DeletePerson(LoginActivity.UserID, pid);
                        }
                    };
                    thread.start();
                    try { thread.join();
                        Toast.makeText(holder.view.getContext(),"그룹 삭제를 성공했습니다!", Toast.LENGTH_SHORT).show();
                    } catch (InterruptedException e) { e.printStackTrace(); }
                }
                else{
                    for(int gid : gidList){
                        Group group = new Group();
                        DBThread.SelectGroupByGidThraed th= new DBThread.SelectGroupByGidThraed(gid,group);
                        th.start();
                        try { th.join(); } catch (InterruptedException e) { e.printStackTrace(); }
                        ArrayList<Integer> pidList = group.getPersonIDList();
                        pidList.remove(new Integer(pid));
                        group.setPersonIDList(pidList);
                        //그룹 인원이 0이되면 그룹을 아예 지운다
                        if(pidList.size()==0){
                            Log.e("pidList.size() is 0", pidList.size()+"");
                            DBThread.DeleteGroupThraed th2 = new DBThread.DeleteGroupThraed(group);
                            th2.start();
                            try { th2.join(); } catch (InterruptedException e) { e.printStackTrace(); }


                            Thread thread = new Thread(){
                                @Override
                                public void run() {
                                    new CommServer(holder.view.getContext()).DeleteGroup(LoginActivity.UserID, group.getGid(), pid);
                                }
                            };
                            thread.start();
                            try { thread.join();
                                Toast.makeText(holder.view.getContext(),"그룹 삭제를 성공했습니다!", Toast.LENGTH_SHORT).show();
                            } catch (InterruptedException e) { e.printStackTrace(); }

                        }
                        //그룹 인원을 변경한 후 DB에 적용한다
                        else{
                            Log.e("pidList.size() is", pidList.size()+"");
                            DBThread.UpdateGroupThraed th2 = new DBThread.UpdateGroupThraed(group);
                            th2.start();
                            try { th2.join(); } catch (InterruptedException e) { e.printStackTrace(); }
                            Thread thread2 = new Thread(){
                                @Override
                                public void run() {
                                    new CommServer(holder.view.getContext()).postEditGroup(LoginActivity.UserID, group.getGid(), pidList, pid);
                                }
                            };
                            thread2.start();
                            try { thread2.join();
                                Toast.makeText(holder.view.getContext(),"그룹 편집을 성공했습니다!", Toast.LENGTH_SHORT).show();
                            } catch (InterruptedException e) { e.printStackTrace(); }


                        }
                    }


                }

                ////////////////////////////////////////////////////////////////////////////////////////////////////////

                //현재 삭제버튼 눌린 person데이터를 지움
                DBThread.DeletePersonThraed t1 = new DBThread.DeletePersonThraed(personList.get(position));
                //꼭 삭제하고 리스트뷰 갱신을 위해 personList를 바뀐 DB로 재갱신 해줘야함!
                DBThread.SelectPersonThraed t2 = new DBThread.SelectPersonThraed(personList);
                t1.start();
                try { t1.join(); } catch (InterruptedException e) { e.printStackTrace(); }
                t2.start();
                try { t2.join(); } catch (InterruptedException e) { e.printStackTrace(); }
                notifyDataSetChanged();

                //인물리스트가 바뀌었으니  그룹리스트뷰도 새로고침 해준다.
                //TODO:: 인물 폴더 삭제 , 그룹 CASCADE, 그룹 인원0면 아예삭제 코드 서버에서도 구현해서 연결해주기
                ((MainActivity)MainActivity.CONTEXT).onResume();

//                for(int i =0; i<gidListInpid.size();i++){
//
//                    new CommServer(holder.view.getContext()).DeletePerson(LoginActivity.UserID, gidListInpid, result.personIDList);
//
//                }
                Toast.makeText(holder.view.getContext(),"사람삭제를 성공했습니다!", Toast.LENGTH_SHORT).show();

            }
        });

    }

    @Override
    public int getItemCount() {
        return personList.size();
    }

}