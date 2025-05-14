import SwiftUI
import shared

struct CalendarView: View {
    
    var body: some View {
        VmView({
            CalendarVm()
        }) { vm, state in
            CalendarViewInner(
                vm: vm,
                state: state
            )
        }
    }
}

private struct CalendarViewInner: View {
    
    let vm: CalendarVm
    let state: CalendarVm.State
    
    ///
    
    @State private var selectedDay: Int32? = nil
    
    var body: some View {
        
        VStack {
            
            VStack {
                
                HStack {
                    
                    ForEach(state.weekTitles, id: \.self) { weekTitle in
                        HStack {
                            Spacer()
                            Text(weekTitle.title)
                                .font(.system(size: 11))
                                .foregroundColor(weekTitle.isBusiness ? .primary: .secondary)
                            Spacer()
                        }
                    }
                }
                .padding(.top, 8)
                
                Divider()
                    .padding(.top, 2.0)
            }
            
            ScrollView(showsIndicators: false) {
                
                LazyVStack(spacing: 0) {
                    
                    ForEach(state.months, id: \.self) { month in
                        
                        HStack {
                            
                            ForEach(0..<month.emptyStartDaysCount, id: \.self) { _ in
                                ZStack {
                                }
                                .frame(minWidth: 0, maxWidth: .infinity)
                            }
                            
                            Text(month.title)
                                .frame(minWidth: 0, maxWidth: .infinity)
                                .foregroundColor(c.white)
                                .padding(.top, 24)
                                .padding(.bottom, 8)
                                .font(.system(size: 17, weight: .bold))
                            
                            ForEach(0..<month.emptyEndDaysCount, id: \.self) { _ in
                                ZStack {
                                }
                                .frame(minWidth: 0, maxWidth: .infinity)
                            }
                        }
                        
                        let weeks = month.weeks as! [[CalendarVm.MonthDay?]]
                        
                        ForEach(weeks, id: \.self) { week in
                            
                            HStack {
                                
                                ForEach(week, id: \.self) { day in
                                    
                                    if let day = day {

                                        let bgColor: Color = {
                                            if day.unixDay == selectedDay {
                                                return .blue
                                            } else if day.isToday {
                                                return .purple
                                            }
                                            return .clear
                                        }()
                                        
                                        Button(
                                            action: {
                                                selectedDay = day.unixDay
                                            },
                                            label: {
                                                
                                                VStack {
                                                    
                                                    Divider()
                                                    
                                                    Text(day.title)
                                                        .foregroundColor(day.isBusiness ? .white : .secondary)
                                                        .padding(.top, 6)
                                                    
                                                    ForEach(day.previews, id: \.self) { preview in
                                                        
                                                        Text(preview)
                                                            .padding(.horizontal, 1)
                                                            .foregroundColor(.secondary)
                                                            .font(.system(size: 11, weight: .light))
                                                            .lineLimit(1)
                                                    }
                                                }
                                                .padding(.bottom, 2)
                                                .frame(minWidth: 0, maxWidth: .infinity)
                                            }
                                        )
                                        .background(bgColor)
                                    } else {
                                        ZStack {
                                        }
                                        .frame(minWidth: 0, maxWidth: .infinity)
                                    }
                                }
                            }
                            
                            let selectedDayLocal = week.first { item in
                                if let item = item, let selectedDay {
                                    return item.unixDay == selectedDay
                                }
                                return false
                            }
                            
                            let isDaySelected: Bool = selectedDayLocal != nil
                            if isDaySelected {
                                EventsCalendarDayView(unixDay: selectedDayLocal!!.unixDay.toInt())
                                // Force update on selectedDay changes
                                    .id("EventsCalendarDayView_\(selectedDayLocal!!.unixDay)")
                            }
                        }
                    }
                }
            }
        }
        .padding(.horizontal, H_PADDING)
        .padding(.top, 6)
    }
}
