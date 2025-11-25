import numpy as np
import matplotlib.pyplot as plt
from matplotlib.widgets import TextBox, Button

def logistic_map(r, x):
    """Logistic equation: x_{n+1} = r * x * (1 - x)"""
    return r * x * (1 - x)

def draw_bifurcation(r_min, r_max, num_r=1000, num_iterations=1000, num_last=100):
    """Draws the bifurcation diagram."""
    r_values = np.linspace(r_min, r_max, num_r)
    x = 0.5 * np.ones(num_r) 

    ax.clear()
    ax.set_title("Bifurcation Diagram")
    ax.set_xlabel("r (Growth rate)")
    ax.set_ylabel("x (Population)")
    ax.set_xlim(r_min, r_max)
    ax.set_ylim(0, 1)

    for i in range(num_iterations):
        x = logistic_map(r_values, x)  
        if i >= (num_iterations - num_last):  
            ax.plot(r_values, x, ',k', alpha=0.25)

    plt.draw()

def submit_values(event=None):
    """Callback function to redraw the plot with updated values."""
    try:
        r_min = float(r_min_box.text)
        r_max = float(r_max_box.text)
        num_iterations = int(num_iterations_box.text)
        draw_bifurcation(r_min, r_max, num_iterations=num_iterations)
    except ValueError:
        print("Invalid input. Please enter numeric values.")

fig, ax = plt.subplots(figsize=(10, 6))
plt.subplots_adjust(bottom=0.3)

initial_r_min = 2.5
initial_r_max = 4.0
initial_num_iterations = 1000
draw_bifurcation(initial_r_min, initial_r_max, num_iterations=initial_num_iterations)

ax_r_min = plt.axes([0.1, 0.15, 0.2, 0.05])
ax_r_max = plt.axes([0.4, 0.15, 0.2, 0.05])
ax_num_iterations = plt.axes([0.7, 0.15, 0.2, 0.05])

r_min_box = TextBox(ax_r_min, "r Min:", initial=str(initial_r_min))
r_max_box = TextBox(ax_r_max, "r Max:", initial=str(initial_r_max))
num_iterations_box = TextBox(ax_num_iterations, "Iterations:", initial=str(initial_num_iterations))

ax_button = plt.axes([0.4, 0.05, 0.2, 0.075])
button = Button(ax_button, "Redraw")

r_min_box.on_submit(submit_values)
r_max_box.on_submit(submit_values)
num_iterations_box.on_submit(submit_values)
button.on_clicked(submit_values)

plt.show()
