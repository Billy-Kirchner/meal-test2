package org.launchcode.mealplanner.controllers;


import org.launchcode.mealplanner.models.Component;
import org.launchcode.mealplanner.models.Ingredient;
import org.launchcode.mealplanner.models.Meal;
import org.launchcode.mealplanner.models.data.ComponentDao;
import org.launchcode.mealplanner.models.data.IngredientDao;
import org.launchcode.mealplanner.models.data.MealDao;
import org.launchcode.mealplanner.models.forms.BuildMealForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.info.ProjectInfoProperties;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.validation.Valid;

@Controller
@RequestMapping("meal")
public class MealController {

    @Autowired
    private MealDao mealDao;

    @Autowired
    private IngredientDao ingredientDao;

    @Autowired
    private ComponentDao componentDao;

    @RequestMapping(value = "")
    public String index(Model model) {

        model.addAttribute("meals", mealDao.findAll());
        model.addAttribute("title", "Meals");

        return "meal/index";
    }


    @RequestMapping(value = "create", method = RequestMethod.GET)
    public String displayCreateMealForm(Model model) {

        model.addAttribute("title", "Create New Meal");
        model.addAttribute(new Meal());

        return "meal/create";
    }


    @RequestMapping(value = "create", method = RequestMethod.POST)
    public String processCreateMealForm(@ModelAttribute @Valid Meal newMeal, Errors errors, Model model) {

        if (errors.hasErrors()) {
            model.addAttribute("title", "Create New Meal");
            return "meal/create";
        }

        mealDao.save(newMeal);



        return "redirect:/meal/build/" + newMeal.getId();
    }

    @RequestMapping(value = "build/{id}", method = RequestMethod.GET)
    public String displayBuildMealForm(Model model, @PathVariable(value="id") int id) {

        BuildMealForm buildMealForm = new BuildMealForm(mealDao.findById(id).orElse(null), ingredientDao.findAll());

        model.addAttribute("form", buildMealForm);
        model.addAttribute("title", "Build Meal: " + mealDao.findById(id).orElse(null).getName());

        return "meal/build";

    }

    @RequestMapping(value = "build/{id}", method = RequestMethod.POST)
    public String processBuildMealForm(Model model, @ModelAttribute @Valid BuildMealForm form, Errors errors) {


        if (errors.hasErrors()){
            model.addAttribute("title", "Build Meal: " + mealDao.findById(form.getMealId()).orElse(null).getName());
            return "meal/build";
        }

        Ingredient newIngredient = ingredientDao.findById(form.getIngredientId()).orElse(null);
        Meal currentMeal = mealDao.findById(form.getMealId()).orElse(null);
        Double servings = form.getServings();
        Component newComponent = new Component(newIngredient, servings);
        componentDao.save(newComponent);
//        currentMeal.addIngredient(newIngredient);
        currentMeal.addComponent(newComponent);
        currentMeal.calculateTotals();
        mealDao.save(currentMeal);


/*        currentMeal.addComponent(newIngredient, servings);
        mealDao.save(currentMeal);*/


        return "redirect:/meal/build/" + currentMeal.getId();

    }

    @RequestMapping(value = "delete/{id}", method = RequestMethod.GET)
    public String deleteMeal( Model model, @PathVariable(value ="id") int id) {

        model.addAttribute("meal", mealDao.findById(id).orElse(null));

        mealDao.deleteById(id);

        return "meal/delete";
    }
}
