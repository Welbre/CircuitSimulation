import pandas as pd
import plotly.express as px

df = pd.read_csv("C:\\Users\\welbre\\Desktop\\mcm\\ElectricalSim\\ggggkkkkkk.csv")

fig = px.line(df, x = 'Time', y=df.columns[0:-1])
fig.show()