library(ggplot2)

response_times = read.csv('response_times.csv', header = TRUE, sep = ',')
ggplot(data = response_times, aes(x = QuerySequence, y = ResponseTime, color = Type)) + geom_point()

total = read.csv('1.csv', header = TRUE, sep = ',')
total$projection_count <- factor(1)
numFiles <- 9
for (i in 2:numFiles) {
  temp = read.csv(paste(i, '.csv', sep =''), header = TRUE, sep = ',')
  temp$projection_count <- factor(i)
  total <- rbind(total, temp)
}

ggplot(data = total, aes(x = projection_count)) +  geom_jitter(aes(y = ResponseTime, color = Type))
ggplot(data = subset(total, QuerySequence != 1), aes(x = projection_count)) +  geom_line(aes(y = ResponseTime, color = Type))
ggplot(data = subset(total, QuerySequence == 1), aes(x = projection_count)) +  geom_line(aes(y = ResponseTime, color = Type))
ggplot(data = subset(total, QuerySequence == 100), aes(x = projection_count)) +  geom_line(aes(y = ResponseTime, color = Type))

ggplot(data = subset(total, QuerySequence == 1), aes(x = projection_count, y = ResponseTime, group = Type, color = Type)) + geom_line()
ggplot(data = subset(total, QuerySequence == 1)) + geom_boxplot(aes(x = projection_count, y = ResponseTime, color = Type))

ggplot(data = subset(total, QuerySequence == 1), aes(projection_count, ResponseTime, fill = Type)) + geom_bar(position = 'dodge', stat = 'identity')

ggplot(data = subset(total, QuerySequence != 1), aes(x = projection_count)) +  geom_jitter(aes(y = ResponseTime, color = Type)) + geom_line(data = subset(total, QuerySequence == 1), aes(x = projection_count, y = ResponseTime, group = Type, color = Type)) + labs(title = "Tuple Reconstruction Costs", x = "# of tuple reconstructions (projected attributes)", y = "Response time (microseconds)") + theme(plot.title = element_text(hjust = 0.5))


stotal = read.csv('S1.csv', header = TRUE, sep = ',')
stotal$selectivity <- factor(1)
for (i in c(10, 100, 1000, 10000)) {
  temp = read.csv(paste('S', i, '.csv', sep =''), header = TRUE, sep = ',')
  temp$selectivity <- factor(i)
  stotal <- rbind(stotal, temp)
}

ggplot(subset(stotal, Type == 'CRACKED')) + geom_smooth(aes(x = QuerySequence, y = ResponseTime, color = selectivity), se = FALSE, method = loess) + labs(title = "Varying Selectivity", x = "Query Sequence", y = "Response time (microseconds)") + theme(plot.title = element_text(hjust = 0.5))

cracked <- aggregate(ResponseTime ~ selectivity, subset(stotal, Type == 'CRACKED'), sum)
cracked$type <- 'CRACKED'
sorted <- aggregate(ResponseTime ~ selectivity, subset(stotal, Type == 'SORTED'), sum)
sorted$type <- 'SORTED' 
cs <- rbind(cracked, sorted)

ggplot(data = cs, aes(selectivity, ResponseTime, fill = type)) + geom_bar(position = 'dodge', stat = 'identity') + labs(title = "Varying Selectivity", x = "Selectivity", y = "Aggregate response time (microseconds)") + theme(plot.title = element_text(hjust = 0.5))


acc = read.csv('acc.csv', header = TRUE, sep = ',')
ggplot(data = acc, aes(QuerySequence, ResponseTime, color = Type)) + geom_smooth(method = loess, se = FALSE)