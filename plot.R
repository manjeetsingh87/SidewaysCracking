library(ggplot2)

response_times = read.csv('response_times.csv', header = TRUE, sep = ',')
ggplot(data = response_times, aes(x = QuerySequence, y = ResponseTime, color = Type)) + geom_point()

