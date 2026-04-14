import SwiftUI
import shared

private let barsHeaderHeight = 28.0
private let hPadding = 8.0

struct SummarySheet: View {
    
    let vm: SummaryVm
    let state: SummaryVm.State
    
    ///
    
    @State private var isChartVisible: Bool = false
    
    var body: some View {
        
        ZStack {
            
            HStack {
                
                //
                // Left Part
                
                ZStack {
                    
                    //
                    // Bars Time Sheet
                    
                    VStack {
                        ForEachIndexed(state.barsTimeRows) { _, barString in
                            VStack(alignment: .leading) {
                                Spacer()
                                Text(barString)
                                    .foregroundColor(.secondary)
                                    .font(.system(size: 10, weight: .light))
                                    .padding(.bottom, 4)
                                Divider()
                            }
                            .padding(.leading, hPadding)
                            .padding(.trailing, 4)
                            Spacer()
                        }
                    }
                    .padding(.top, barsHeaderHeight)
                    
                    //
                    // Bars
                    
                    GeometryReader { geometry in
                        
                        ScrollView(.horizontal) {
                            
                            HStack {
                                
                                Spacer()
                                
                                ForEachIndexed(state.daysBarsUi.reversed()) { _, dayBarsUi in
                                    
                                    VStack {
                                        
                                        VStack {
                                            
                                            Spacer()
                                            
                                            Text(dayBarsUi.dayString)
                                                .lineLimit(1)
                                                .foregroundColor(.secondary)
                                                .font(.system(size: 10, weight: .light))
                                        }
                                        .padding(.bottom, 8)
                                        .frame(height: barsHeaderHeight)
                                        
                                        GeometryReader { geometry in
                                            VStack {
                                                ForEachIndexed(dayBarsUi.barsUi) { _, barUi in
                                                    ZStack {}
                                                        .frame(minWidth: 0, maxWidth: .infinity)
                                                        .frame(height: CGFloat(barUi.ratio) * geometry.size.height)
                                                        .background(barUi.activityDb?.colorRgba.toColor() ?? Color(.systemGray5))
                                                }
                                            }
                                            .clipShape(roundedShape)
                                            .padding(.horizontal, 4)
                                        }
                                    }
                                    .frame(width: 16)
                                }
                            }
                            .frame(minWidth: geometry.size.width)
                        }
                        .defaultScrollAnchor(.trailing)
                    }
                    .padding(.leading, 28)
                }
                .frame(minWidth: 0, maxWidth: .infinity)
                .padding(.bottom, 56)
                .padding(.trailing, 12)
                
                //
                // Right Part
                
                ScrollView {
                    
                    VStack {
                        
                        ForEachIndexed(state.activitiesUi) { idx, activityUi in
                            GoalView(activityUi: activityUi)
                        }
                        
                        Padding(vertical: 56)
                    }
                    .frame(minWidth: 0, maxWidth: .infinity)
                }
            }

            if isChartVisible {
                SummaryChartView(activitiesUi: state.activitiesUi)
                    .id(state)
            }
        }
        
        /*
        Spacer()
        
        VStack {
            
            HStack {
                
                Button(
                    action: {
                        isChartVisible.toggle()
                    },
                    label: {
                        HStack {
                            Image(systemName: "chart.pie")
                                .font(.system(
                                    size: isChartVisible ? 20 : 22,
                                    weight: .light
                                ))
                                .foregroundColor(isChartVisible ? .white : .secondary)
                        }
                        .frame(width: 32, height: 32)
                        .background(roundedShape.fill(isChartVisible ? .blue : .clear))
                    }
                )
            }
            .padding(.top, 10)
            .padding(.horizontal, 16)
        }
        */
    }
}

private struct GoalSecondaryText: View {
    
    let text: String
    
    var body: some View {
        Text(text)
            .font(.system(size: 12, weight: .light))
            .lineLimit(1)
            .foregroundColor(.secondary)
    }
}

private struct GoalView: View {
    
    let activityUi: SummaryVm.ActivityUi
    
    ///
    
    private var goalColor: Color {
        activityUi.activityDb.colorRgba.toColor()
    }
    
    var body: some View {
        
        VStack {
            
            HStack {
                
                GoalSecondaryText(text: activityUi.perDayString)
                
                Spacer()
                
                GoalSecondaryText(text: activityUi.totalTimeString)
            }
            
            HStack {
                
                Text(activityUi.title)
                    .padding(.trailing, 4)
                    .foregroundColor(.primary)
                    .font(.system(size: 14, weight: .medium))
                    .lineLimit(1)
                
                Spacer()
                
                GoalSecondaryText(text: activityUi.percentageString)
            }
            .padding(.top, 4)
            
            
            HStack {
                
                ZStack {
                    
                    GeometryReader { geometry in
                        
                        ZStack {}
                            .frame(maxHeight: .infinity)
                            .frame(width: geometry.size.width * Double(activityUi.ratio))
                            .background(goalColor)
                    }
                    .fillMaxWidth()
                }
                .frame(height: 8)
                .frame(minWidth: 0, maxWidth: .infinity)
                .background(Color(.systemGray5))
                .clipShape(roundedShape)
                
                Padding(horizontal: 4)
                
                ZStack {}
                    .frame(width: 8, height: 8)
                    .background(roundedShape.fill(goalColor))
            }
            .padding(.top, 6)
        }
        .padding(.top, 7)
        .padding(.trailing, hPadding)
        
        if activityUi.children.count > 0 {
            HStack {
                
                VStack {
                    Spacer()
                }
                .frame(width: 2)
                .background(roundedShape.fill(goalColor))
                .padding(.top, 18)

                VStack {
                    ForEachIndexed(activityUi.children as! [SummaryVm.ActivityUi]) { _, childrenActivityUi in
                        GoalView(activityUi: childrenActivityUi)
                    }
                }
                .padding(.leading, 12)
            }
        }
    }
}
