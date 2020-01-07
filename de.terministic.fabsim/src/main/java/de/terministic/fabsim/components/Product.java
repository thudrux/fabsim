package de.terministic.fabsim.components;

public class Product {

	String name;

	public Product(String name, Recipe recipe) {
		this.name = name;
		this.recipe = recipe;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Recipe getRecipe() {
		return recipe;
	}

	public void setRecipe(Recipe recipe) {
		this.recipe = recipe;
	}

	Recipe recipe;

	public String toString() {
		return name;
	}
}
