package com.example.fade;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.fade.entity.Person;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import static android.app.Activity.RESULT_OK;

public class MainDrawerFragment extends Fragment {

    final int CODE_REGI_PERSON = 0;
    PersonAdapter personAdapter;
    ArrayList<Person> personList=new ArrayList<Person>();
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
                getActivity().finish();
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

        Button btn_subPerson = holder.view.findViewById(R.id.btn_subPerson);
        btn_subPerson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DBThread.DeletePersonThraed t1 = new DBThread.DeletePersonThraed(personList.get(position));
                //꼭 삭제하고 리스트뷰 갱신을 위해 personList를 바뀐 DB로 재갱신 해줘야함!
                DBThread.SelectPersonThraed t2 = new DBThread.SelectPersonThraed(personList);
                t1.start();
                try { t1.join(); } catch (InterruptedException e) { e.printStackTrace(); }
                t2.start();
                try { t2.join(); } catch (InterruptedException e) { e.printStackTrace(); }
                notifyDataSetChanged();
                //인물리스트가 바뀌었으니 삭제되었을 것을 대비해 그룹리스트뷰도 새로고침 해준다.
                //TODO::인물리스트에서 삭제된 애는 CASCADE해줌 (그룹리스트에서도 삭제해줌) 만약 그룹리스트가 사이즈가 0이면 그룹삭제해줌
                ((MainActivity)MainActivity.CONTEXT).onResume();
            }
        });

    }

    @Override
    public int getItemCount() {
        return personList.size();
    }

}