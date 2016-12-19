library(ggplot2)
seconds = function(x) { x/1000000000}


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
total$ResponseTime <- as.numeric(total$ResponseTime)

ggplot(data = total, aes(x = projection_count)) +  geom_jitter(aes(y = ResponseTime, color = Type))
ggplot(data = subset(total, QuerySequence != 1), aes(x = projection_count)) +  geom_line(aes(y = ResponseTime, color = Type))
ggplot(data = subset(total, QuerySequence == 1), aes(x = projection_count)) +  geom_line(aes(y = ResponseTime, color = Type))
ggplot(data = subset(total, QuerySequence == 100), aes(x = projection_count)) +  geom_line(aes(y = ResponseTime, color = Type))

ggplot(data = subset(total, QuerySequence == 1), aes(x = projection_count, y = ResponseTime, group = Type, color = Type)) + geom_line()
ggplot(data = subset(total, QuerySequence == 1)) + geom_boxplot(aes(x = projection_count, y = ResponseTime, color = Type))

ggplot(data = subset(total, QuerySequence == 1), aes(projection_count, ResponseTime, fill = Type)) + geom_bar(position = 'dodge', stat = 'identity')

q <- ggplot(data = subset(total, QuerySequence != 1), aes(x = projection_count))
q <- q + geom_point(aes(y = ResponseTime, color = QuerySequence))  + scale_colour_gradientn(colours=rainbow(8))
q <- q + geom_line(data = subset(total, QuerySequence == 1), aes(x = projection_count, y = ResponseTime, group = Type, color = Type)) 
q <- q + labs(title = "Tuple Reconstruction Costs", x = "# of tuple reconstructions (projected attributes)", y = "Response time") + theme(plot.title = element_text(hjust = 0.5))
q

g <- ggplot(total, aes(projection_count, ResponseTime, group = Type, shape = Type)) + scale_shape_manual(values = c(4, 19))
g <- g + geom_jitter(data = subset(total, QuerySequence != 1 & Type == 'CRACKED'), aes(group = Type, color = QuerySequence)) 
g <- g + geom_point(data = subset(total, QuerySequence == 1), aes(projection_count, ResponseTime, group = Type, color = QuerySequence))
g <- g + geom_line(data = subset(total, QuerySequence == 1), aes(projection_count, ResponseTime, group = Type), linetype =2)
g <- g + labs(title = "Tuple Reconstruction Costs", x = "# of tuple reconstructions (projected attributes)", y = "Response time (sec)") + theme(plot.title = element_text(hjust = 0.5))+ scale_y_continuous(labels = seconds);
g


d <- aggregate(ResponseTime ~ Type+projection_count, total, sum)
h <- ggplot(d, aes(x = projection_count, y = ResponseTime, fill = Type)) + geom_bar(stat = 'identity', position = position_dodge())
h <- h + labs(title = "Tuple Reconstruction Costs", x = "# of tuple reconstructions (projected attributes)", y = "Aggregate response time (sec)") + theme(plot.title = element_text(hjust = 0.5))+ scale_y_continuous(labels = seconds);
h

etotal = read.csv('2.csv', header = TRUE, sep = ',')
etotal$QuerySequence <- factor(etotal$QuerySequence)
QS <- c(1, 2, 6, 8, 9, 100, 500, 1000)
ggplot(data = subset(etotal, QuerySequence %in% QS), aes(x = QuerySequence, y = ResponseTime, color = Type)) + geom_boxplot() + geom_point() + labs(title = "Variance in 10 runs", x = "Query Sequence", y = "Response time (sec)") + theme(plot.title = element_text(hjust = 0.5))+ scale_y_continuous(labels = seconds);

stotal = read.csv('S10.csv', header = TRUE, sep = ',')
stotal$selectivity <- factor(10)
for (i in c(100, 1000, 10000, 100000)) {
  temp = read.csv(paste('S', i, '.csv', sep =''), header = TRUE, sep = ',')
  temp$selectivity <- factor(i)
  stotal <- rbind(stotal, temp)
}

ggplot(stotal) + geom_point(aes(x = QuerySequence, y = ResponseTime, color = selectivity)) + labs(title = "Varying Selectivity", x = "Query Sequence", y = "Aggregated response time (secs)") + scale_y_continuous(labels = seconds) + theme(plot.title = element_text(hjust = 0.5))


cracked <- aggregate(ResponseTime ~ selectivity, subset(stotal, Type == 'CRACKED'), sum)
cracked$type <- 'CRACKED'
sorted <- aggregate(ResponseTime ~ selectivity, subset(stotal, Type == 'SORTED'), sum)
sorted$type <- 'SORTED' 
cs <- rbind(cracked, sorted)

ggplot(data = cs, aes(selectivity, ResponseTime, fill = type)) + geom_bar(position = 'dodge', stat = 'identity') + labs(title = "Varying Selectivity", x = "Selectivity", y = "Aggregate response time (microseconds)") + theme(plot.title = element_text(hjust = 0.5))


seconds = function(x) { x/1000000000}

acc = read.csv('accCRACKED.csv', header = TRUE, sep = ',')
acc <- rbind(acc, read.csv('accHYBRID.csv', header = TRUE, sep = ','))
acc <- rbind(acc, read.csv('accSORTED.csv', header = TRUE, sep = ','))
ggplot(data = acc, aes(QuerySequence, ResponseTime, color = Type)) + geom_point()  + scale_y_continuous(labels = seconds) + labs(title = expression("Crack vs Hybrid vs Sorted (N = "* 10^7*", Selectivity ="* 10^4*", CS_Threshold = "* 10^4*")"), y = "Aggregate response time (sec)")+ theme(plot.title = element_text(hjust = 0.5))


ttotal = read.csv('T10.csv', header = TRUE, sep = ',')
ttotal$threshold <- factor(10)
for (i in c('100', '1000', '10000', '100000', '1000000')) {
  temp = read.csv(paste('T', i, '.csv', sep =''), header = TRUE, sep = ',')
  temp$threshold <- factor(i)
  ttotal <- rbind(ttotal, temp)
}
seconds = function(x) { x/1000000000}

ggplot(data = ttotal, aes(QuerySequence, ResponseTime, color = threshold)) + geom_point() + scale_y_continuous(labels = seconds) + labs(title = "Hybrid CS Varying Threshold (selectivity = 100000)", y = "Aggregate response time (sec)")+ theme(plot.title = element_text(hjust = 0.5))