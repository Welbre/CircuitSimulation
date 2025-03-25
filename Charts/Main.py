import pandas as pd
import plotly.express as px
import sys

df = pd.read_csv(sys.argv[1])

fig = px.line(df, x = df.columns[0], y=df.columns[0:-1])
fig.show()