/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlet;

import annotation.ClassAnnotation;
import annotation.MethodAnnotation.urlMethod;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import modelview.ModelView;
import org.reflections.Reflections;

/**
 *
 * @author Tsiory
 */
public class FrontController extends HttpServlet {

    Class classes;
    Class modelClass;
    private final String set = "set";
    private final String get = "get";
    private Method setMethod;
    private String className;
    private final HashMap<String, String> methodUrl = new HashMap<>();
    private final String FICHIER_FOLDER = "/image";
    private String uploadPath;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, ClassNotFoundException, NoSuchFieldException, NoSuchMethodException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            String[] urls = request.getRequestURI().split("/");
            String urlfinal = "";

            this.setUrl("controller");

            for (Map.Entry<String, String> entry : this.methodUrl.entrySet()) {
                String clienturl = request.getRequestURI().split("/")[urls.length - 1];
                if (clienturl.equals(entry.getKey())) {
                    urlfinal = entry.getValue().replace(',', '-');
                } else if (clienturl.equals(entry.getValue().replace(',', '-') + ".do")) {
                    urlfinal = clienturl.split("\\.")[0];
                }
            }

            if (this.verif_url(urlfinal)) {
                String methodes = this.getMethode(urlfinal);
                this.className = this.getClasses(urlfinal);
                this.classes = Class.forName("controller." + this.concatClass(this.className));
                this.modelClass = Class.forName("model." + this.className);

                Method[] listeMethodes = this.classes.getMethods();

                Field objet = this.classes.getDeclaredField(Character.toLowerCase(this.getClasses(urlfinal).charAt(0)) + this.getClasses(urlfinal).substring(1));

                Class attribut = objet.getType();
                HashMap<String, String> listeAttributs = this.getParametersList(request);
                Object instance = this.classes.newInstance();
                Object instance_model = this.modelClass.newInstance();

                if (!listeAttributs.isEmpty()) {
                    FrontController.setAttributClass(listeAttributs, instance_model);
                    setModelController(instance, objet, instance_model);
                }

                for (Method method : listeMethodes) {
                    if (this.checkMethode(classes, methodes, method)) {
                        ModelView view = (ModelView) method.invoke(instance);
                        request.setAttribute("data", view.getHash());
                        RequestDispatcher dispatch = request.getRequestDispatcher("/view/" + className + "/" + view.getPage() + ".jsp");
                        dispatch.forward(request, response);
                    }
                }
            } else if (!this.verif_url(urlfinal)) {
                response.sendRedirect("Erreur/404.jsp");
            }
        } catch (ClassNotFoundException | IllegalArgumentException | IllegalAccessException | InvocationTargetException | InstantiationException ex) {
            System.out.println(ex.getMessage());
        } catch (Exception ex) {
            Logger.getLogger(FrontController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void setModelController(Object model, Field attribut, Object value) throws Exception {
        Class models = model.getClass();
        Method method = recupererSetter(models, attribut);
        Object[] param = new Object[1];
        param[0] = value;
        Object retourFonction = method.invoke(model, param);
    }

    public Object castvalue(Class type, Object value) {
        if (type.getSimpleName().equalsIgnoreCase("String")) {
            return (String) value;
        }
        if (type.getSimpleName().equalsIgnoreCase("Date")) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                return dateFormat.parse((String) value);
            } catch (ParseException ex) {
                Logger.getLogger(FrontController.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (type.getSimpleName().equalsIgnoreCase("Int")) {
            return Integer.valueOf((String) value);
        } else if (type.getSimpleName().equalsIgnoreCase("Double")) {
            return Double.valueOf((String) value);
        } else if (type.getSimpleName().equalsIgnoreCase("Float")) {
            return Float.valueOf((String) value);
        } else if (type.getSimpleName().equalsIgnoreCase("Part")) {
            return (Part) value;
        }
        return null;
    }

    private List<String> getParameters(HttpServletRequest request) {
        Enumeration<String> listeParametre = request.getParameterNames();
        List<String> retour = new ArrayList<>();
        while (listeParametre.hasMoreElements()) {
            if (request.getContentType() != null) {
                String parameterName = listeParametre.nextElement();
                retour.add(parameterName);
                if (request.getContentType().contains(";")) {
                    retour.add("ProcesVerbale.fichier");
                }
            }
        }
        System.out.println("Retour " + retour);
        return retour;
    }

    private List<String> getParametersPart(HttpServletRequest request) {
        try {
            Collection<Part> parameters = request.getParts();
            List<String> retour = new ArrayList<>();
            for (Part l : parameters) {
                retour.add(l.getName());
            }
            return retour;
        } catch (IOException ex) {
            Logger.getLogger(FrontController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServletException ex) {
            Logger.getLogger(FrontController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public boolean verif_url(String url) {
        return url.indexOf('-') != 0;
    }

    public String getMethode(String url) {
        String[] urls = url.split("-");
        return urls[0];
    }

    public String getClasses(String url) {
        String[] urls = url.split("-");
        return urls[1];
    }

    public String concatClass(String classes) {
        return classes + "Controller";
    }

    public boolean checkMethode(Class classes, String methodes, Method method) {
        return method.getName().equals(methodes);
    }

    private HashMap<String, String> getParametersList(HttpServletRequest request) {
        HashMap<String, String> retour = new HashMap<>();
        List<String> parameterList = this.getParameters(request);
        String filename = "";
        parameterList.forEach((parameter) -> {
            if (parameter.split("\\.")[1].equals("fichier")) {
                try {
                    Part part = request.getPart(parameter);
                    String nomfichier = this.upload(part);
                    retour.put(parameter.split("\\.")[1], "'" + nomfichier + "'");
                } catch (IOException ex) {
                    Logger.getLogger(FrontController.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ServletException ex) {
                    Logger.getLogger(FrontController.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                retour.put(parameter.split("\\.")[1], request.getParameter(parameter));
            }
        });
        return retour;
    }

    public static String toUpper(String attribut) {
        char[] spliter = attribut.toCharArray();
        char[] alphabetMin = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
        char[] alphabetMaj = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
        for (int j = 0; j < alphabetMin.length; j++) {
            if (spliter[0] == alphabetMin[j]) {
                spliter[0] = alphabetMaj[j];
            }
        }
        return String.valueOf(spliter);
    }

    Set<Class<?>> getListeClassAnnoted(String packageName) {
        Reflections reflect = new Reflections(packageName);
        Set<Class<?>> retour = reflect.getTypesAnnotatedWith(ClassAnnotation.class);
        return retour;
    }

    public void setUrl(String packages) {
        Set<Class<?>> listeClassAnnoted = this.getListeClassAnnoted(packages);
        listeClassAnnoted.forEach((Class<?> classAnnoted) -> {
            for (Method method : classAnnoted.getMethods()) {
                urlMethod annoted = method.getAnnotation(urlMethod.class);
                if (annoted != null) {
                    this.methodUrl.put(annoted.url(), annoted.method() + "," + annoted.className());
                }
            }
        });
    }

    public static void setAttributClass(HashMap donnees, Object objet) throws Exception {
        try {
            // parcrous des attributs de l'objet
            Field[] attributs = objet.getClass().getDeclaredFields();
            for (int i = 0; i < attributs.length; i++) {
                Object valeur = donnees.get(attributs[i].getName());
                if (valeur != null) {
                    ajouterValeur(objet, valeur, attributs[i]);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println(ex.getMessage());
            ex = new Exception("Aucune valeur assigné à l'objet");
            throw ex;
        }
    }

    private static void ajouterValeur(Object objet, Object valeur, Field attribut) throws Exception {
        Class classeObjet = objet.getClass();

        // recuperation du settter de l'attribut
        Method setter = null;
        try {
            setter = recupererSetter(classeObjet, attribut);
        } catch (Exception ex) {
            ex = new Exception("Setter non trouvée");
            throw ex;
        }

        // assignation de la valeur
        try {
            Object[] param = new Object[1];
            param[0] = castValeur(valeur, attribut, objet);
            Object retourFonction = setter.invoke(objet, param);
            String a = "";
            String b = "";
        } catch (Exception ex) {
            ex.printStackTrace();
            ex = new Exception("Valeur non assigné");
            throw ex;
        }
    }

    private static Object castValeur(Object valeur, Field attribut, Object objet) throws Exception {
        Object retour = null;
        Class classeAttribut = attribut.getType();
        if (classeAttribut == int.class) {
            return Integer.valueOf(String.valueOf(valeur));
        } else if (classeAttribut == java.sql.Date.class) {
            java.sql.Date date = java.sql.Date.valueOf(String.valueOf(valeur));
            return date;
        } else if (classeAttribut == String.class) {
            return String.valueOf(valeur);
        }
        retour = valeur;
        return retour;
    }

    private static Method recupererSetter(Class classeObjet, Field attribut) throws Exception {
        Method retour = null;
        try {
            Class[] classParams = new Class[1];
            classParams[0] = attribut.getType();
            String nomMethode = "set" + upperCaseFirstLetter(attribut.getName());
            retour = classeObjet.getDeclaredMethod(nomMethode, classParams);
        } catch (Exception ex) {
            ex.printStackTrace();
            ex = new Exception("Methode non trouvé");
            throw ex;
        }
        return retour;
    }

    private String upload(Part part) throws IOException {
        this.uploadPath = "E:\\Etude\\S5\\Framework\\Framework\\GestionContenu\\web\\fichier";
        File upload_dir = new File(this.uploadPath);
        if (!upload_dir.exists()) {
            upload_dir.mkdir();
        }
        String filename = "file_" + part.getSubmittedFileName();
        String cheminFichier = this.uploadPath + File.separator + filename;
        InputStream is = part.getInputStream();
        Files.copy(is, Paths.get(cheminFichier), StandardCopyOption.REPLACE_EXISTING);
        return cheminFichier;
    }

    private static String upperCaseFirstLetter(String mot) {
        char[] charNomClasse = mot.toCharArray();
        charNomClasse[0] = Character.toUpperCase(charNomClasse[0]);
        return new String(charNomClasse);
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (ClassNotFoundException | NoSuchFieldException | NoSuchMethodException ex) {
            Logger.getLogger(FrontController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (ClassNotFoundException | NoSuchFieldException | NoSuchMethodException ex) {
            Logger.getLogger(FrontController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Method getSettersMethod(Method[] listeMethode, String attribut) {
        String methodName = "set" + Character.toUpperCase(attribut.charAt(0)) + attribut.substring(0);
        for (Method listeMethode1 : listeMethode) {
            if (listeMethode1.getName().equals(methodName)) {
                return listeMethode1;
            }
        }
        return null;
    }

    public Method getGettersMethod(Method[] listeMethode, String attribut) {
        String methodName = "get" + Character.toUpperCase(attribut.charAt(0)) + attribut.substring(0);
        for (Method listeMethode1 : listeMethode) {
            if (listeMethode1.getName().equals(methodName)) {
                return listeMethode1;
            }
        }
        return null;
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }
    // </editor-fold>
}
