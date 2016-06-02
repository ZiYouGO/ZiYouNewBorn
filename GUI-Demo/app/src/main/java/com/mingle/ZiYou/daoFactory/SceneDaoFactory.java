//package com.mingle.ZiYou.daoFactory;
//
//
//import com.mingle.ZiYou.daoImpl.PointDao;
//import com.mingle.ZiYou.daoImpl.SceneDao;
//import com.mingle.ZiYou.daoInterface.PointInterface;
//import com.mingle.ZiYou.daoInterface.SceneInterface;
//
//public class SceneDaoFactory {
//    public SceneInterface getInstance(String classname)
//    {
//        SceneInterface p = (SceneDao)Class.forName(classname).newInstance();
//        return p;
//    }
//}
