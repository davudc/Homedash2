package com.ftpix.homedash.app;

import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.post;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.ftpix.homedash.app.controllers.*;
import com.ftpix.homedash.models.*;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ftpix.homedash.db.DB;
import com.ftpix.homedash.plugins.Plugin;
import com.ftpix.homedash.utils.Predicates;
import com.google.gson.Gson;

import io.gsonfire.GsonFireBuilder;
import org.eclipse.jetty.http.HttpStatus;
import spark.ModelAndView;
import spark.template.jade.JadeTemplateEngine;

public class Endpoints {
    private static Logger logger = LogManager.getLogger();



    public static void define() {

        LayoutController.getInstance().defineEndpoints();
        ModuleController.getInstance().defineEndpoints();
        PageController.getInstance().defineEndpoints();
        PluginController.getInstance().defineEndpoints();
        SettingsController.getInstance().defineEndpoints();
        ModuleLayoutController.getInstance().defineEndpoints();
        ModuleSettingsController.getInstance().defineEndpoints();



		/*
         * Main Page
		 */
        get("/", (req, res) -> {
            Map<String, Object> model = new HashMap<>();

            try {
                List<Plugin> plugins = PluginModuleMaintainer.getAllPluginInstances();

                // we need to find all the cs and js files to load
                logger.info("Finding all distinct plugins to load distinct JS files and CSS");
                Object[] filteredPlugins = plugins.stream().filter(Predicates.distinctByKey(p -> p.getId())).toArray();

                logger.info("{} plugins, {} distinct plugins", plugins.size(), filteredPlugins.length);
                model.put("filteredPlugins", filteredPlugins);
                model.put("plugins", plugins);
            } catch (Exception e) {
                e.printStackTrace();

            }
            return new ModelAndView(model, "index");

        }, new JadeTemplateEngine());




    }


    /**
     * Plugin resources
     */
    public static void pluginResources() {
        get("/plugin/:name/files/*", (req, res) -> {

            String name = req.params("name");
            String path = req.splat()[0];
            String fullPath = "web/" + name + "/files/" + path;

            logger.info("/plugin/{}/images/{}", name, path);

            try {
                Path p = Paths.get(fullPath);
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                InputStream is = classLoader.getResourceAsStream(fullPath);

                // res.raw().setContentType("text/javascript");
                res.raw().setHeader("Content-Disposition", "attachment; filename=" + p.getFileName());

                byte[] buffer = new byte[1024];
                int len;
                while ((len = is.read(buffer)) > 0) {
                    res.raw().getOutputStream().write(buffer, 0, len);
                }

                is.close();
                return res.raw();
            } catch (Exception e) {
                logger.error("Error while getting resource", e);
                res.status(500);
                return "";
            }
        });

        get("/plugin/:name/js/*", (req, res) -> {

            String name = req.params("name");
            String path = req.splat()[0];
            String fullPath = "web/" + name + "/js/" + path;

            logger.info("/plugin/{}/js/{}", name, path);

            try {
                Path p = Paths.get(fullPath);
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                InputStream is = classLoader.getResourceAsStream(fullPath);

                res.type("text/javascript");
                res.header("Content-Disposition", "attachment; filename=" + p.getFileName());

                String result = IOUtils.toString(is);
                is.close();
                return result;
            } catch (Exception e) {
                logger.error("Error while getting resource", e);
                res.status(500);
                return "";
            }
        });

        get("/plugin/:name/css/*", (req, res) -> {

            String name = req.params("name");
            String path = req.splat()[0];
            String fullPath = "web/" + name + "/css/" + path;

            logger.info("/plugin/{}/css/{}", name, path);

            try {
                Path p = Paths.get(fullPath);
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                InputStream is = classLoader.getResourceAsStream(fullPath);

                res.type("text/css");
                res.header("Content-Disposition", "attachment; filename=" + p.getFileName());

                String result = IOUtils.toString(is);
                is.close();
                return result;
            } catch (Exception e) {
                logger.error("Error while getting resource", e);
                res.status(500);
                return "";
            }
        });

    }
}