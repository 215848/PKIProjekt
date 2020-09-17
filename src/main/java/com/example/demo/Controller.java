package com.example.demo;

import com.example.demo.Security.UserPrincipal;
import org.hibernate.Session;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@org.springframework.stereotype.Controller
@Transactional
public class Controller {

    @PersistenceContext
    private EntityManager entityManager;




    @GetMapping("/")
    public String mainPage(){
        return "redirect:/all";
    }

    @GetMapping("/all")
    public String showAll(Model model, Principal principal, HttpServletRequest request) {
        DatabaseChoice dbChoice=new DatabaseChoice("Db no selected");
        request.getSession().setAttribute("dbChoice",dbChoice);

        Query q=entityManager.createNativeQuery("SELECT table_name " +
                "FROM information_schema.tables " +
                "WHERE table_schema = 'public' " +
                "ORDER BY table_name;");

        List list= (List) q.getResultList();


        model.addAttribute("tables",list);
        String userName= principal.getName();
        model.addAttribute("name",userName);
        model.addAttribute("db",dbChoice);
        return "sides/SelectPage";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model, Principal principal, HttpServletRequest request) {
        DatabaseChoice dbChoice= (DatabaseChoice) request.getSession().getAttribute("dbChoice");
        Query q=entityManager.createNativeQuery("SELECT column_name FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = \'"+ dbChoice.database +"\';");
        CreationList creationList=new CreationList();

        int size=q.getResultList().size();
        for(int i=0;i<size;i++){
                creationList.getValues().add("");
        }

        List tableHeader=new ArrayList();
        tableHeader.add(getColumnNames(dbChoice).toArray());

        model.addAttribute("tableHeader",tableHeader);
        model.addAttribute("creationList", creationList);
        model.addAttribute("mode","CREATE");
        String userName= principal.getName();
        model.addAttribute("name",userName);

        model.addAttribute("db",dbChoice);
        return "sides/CreateRowForm";
    }

    @PostMapping("/save")
    public String saveBooks(@ModelAttribute CreationList creationList, Model model , @RequestParam("mode") String mode, HttpServletRequest request) {
        DatabaseChoice dbChoice= (DatabaseChoice) request.getSession().getAttribute("dbChoice");
        StringBuilder sb=new StringBuilder();
        List<String> colums= getColumnNames(dbChoice);
        if(mode.equals("EDIT")){
            sb.append("update ").append(dbChoice.database).append(" set ");

            for(int i = 1; i<creationList.getValues().size(); i++){
                String s=creationList.getValues().get(i);
                if(s.matches("-?\\d+(\\.\\d+)?")){
                    sb.append(colums.get(i)).append(" = ").append(s);
                }else{
                    sb.append(colums.get(i)).append(" = ").append("'").append(s).append("'");
                }
                sb.append(",");
            }
            sb.replace(sb.lastIndexOf(","),sb.lastIndexOf(",")+1,"");
            sb.append(" where ").append((colums.get(0))).append(" = ").append(creationList.getValues().get(0));
        }else{
            String columnNames="("+colums.stream().skip(1).map(x->"\""+x+"\",").collect(Collectors.joining()).replaceAll(",$",")");
            sb.append("insert into ").append(dbChoice.database).append(columnNames).append(" values (");
            for(String s:creationList.getValues()){
                if(s.isEmpty()){
                    continue;
                }
                if(s.matches("-?\\d+(\\.\\d+)?")){
                    sb.append(s);
                }else{
                    sb.append("'").append(s).append("'");
                }
                sb.append(",");
            }
            sb.replace(sb.lastIndexOf(","),sb.lastIndexOf(",")+1,")");
        }
        System.out.println(sb.toString());
        entityManager.createNativeQuery(sb.toString()).executeUpdate();
        return "redirect:/dbBooks";
    }

    @GetMapping("/edit")
    public String showEditForm(@RequestParam(value = "position", required = true) String position, Model model, Principal principal, HttpServletRequest request) {
        DatabaseChoice dbChoice= (DatabaseChoice) request.getSession().getAttribute("dbChoice");
        Query q=entityManager.createNativeQuery("select * from "+ dbChoice.database);
        List tableBody=new ArrayList();
        tableBody.addAll(q.getResultList());

        CreationList dto=new CreationList();

        int size=((Object[])q.getResultList().get(0)).length;
        for(int i=0;i<size;i++){
             dto.getValues().add(String.valueOf(((Object[])tableBody.get(Integer.valueOf(position)))[i]));
        }

        List tableHeader=new ArrayList();
        tableHeader.add(getColumnNames(dbChoice).toArray());

        model.addAttribute("tableHeader",tableHeader);
        model.addAttribute("db",dbChoice);
        model.addAttribute("creationList", dto);
        model.addAttribute("mode","EDIT");
        String userName= principal.getName();
        model.addAttribute("name",userName);

        return "sides/CreateRowForm";
    }

    @Transactional
    @GetMapping("/delete")
    public String delete(@RequestParam(value = "position", required = true) String position, Model model, HttpServletRequest request) {
        DatabaseChoice dbChoice= (DatabaseChoice) request.getSession().getAttribute("dbChoice");
        Query q=entityManager.createNativeQuery("delete from "+ dbChoice.database+ " where id=" + position);
        q.executeUpdate();
        return "redirect:/dbBooks";
    }



    @GetMapping("/dbBooks")
    public String listBooks(@ModelAttribute DatabaseChoice db,Model model, Principal principal, HttpServletRequest request){
        if(db.database!=null){
            request.getSession().setAttribute("dbChoice",db);
        }else{
            db= (DatabaseChoice) request.getSession().getAttribute("dbChoice");
        }

        Query q=entityManager.createNativeQuery("select * from "+ db.database);

        List tableHeader=new ArrayList();
        tableHeader.add(getColumnNames(db).toArray());

        List tableBody=new ArrayList();
        tableBody.addAll(q.getResultList());

        model.addAttribute("tableBody",tableBody);
        model.addAttribute("tableHeader",tableHeader);
        model.addAttribute("db",db);
        String userName= principal.getName();
        model.addAttribute("name",userName);


        return "sides/TablePage";
    }

    @GetMapping("/customQuery")
    public String customQuery(@RequestParam(value = "query", required = true) String query, Model model, Principal principal, HttpServletRequest request){
        DatabaseChoice dbChoice= (DatabaseChoice) request.getSession().getAttribute("dbChoice");
        Query q=entityManager.createNativeQuery(query);
        model.addAttribute("db",dbChoice);
        String userName= principal.getName();
        model.addAttribute("name",userName);

        System.out.println(query);
        if(query.startsWith("select")){
            List tableBody=new ArrayList();
            tableBody.addAll(q.getResultList());
            String[] headers=query.substring("select ".length(),query.indexOf(" from ")).split(",");
            model.addAttribute("tableBody",tableBody);
            model.addAttribute("tableHeader",headers);
            return "sides/CustomTablePage";
        }else{
            q.executeUpdate();
            return "sides/SuccessQueryPage";
        }
    }
    @GetMapping("/customQueryForm")
    public String customQueryForm(Model model, Principal principal, HttpServletRequest request){
        DatabaseChoice dbChoice= (DatabaseChoice) request.getSession().getAttribute("dbChoice");
        model.addAttribute("db",dbChoice);
        String userName= principal.getName();
        model.addAttribute("name",userName);

        return "sides/CustomQueryForm";
    }

    public List<String> getColumnNames(DatabaseChoice dbChoice){
        Query q=entityManager.createNativeQuery ("SELECT column_name FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = \'"+ dbChoice.database +"\';");

        List<String> columns= (List<String>) q
                .getResultList();
//                .stream()
//                .map(x->((Object[])x)[0])
//                .collect(Collectors.toList());

        return columns;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }


}
