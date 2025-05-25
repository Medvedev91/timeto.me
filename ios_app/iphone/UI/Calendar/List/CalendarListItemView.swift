import SwiftUI
import shared

struct CalendarListItemView: View {
    
    let eventUi: CalendarListVm.EventUi
    let withTopDivider: Bool
    
    ///
    
    @Environment(Navigation.self) private var navigation
    
    var body: some View {
        
        ZStack(alignment: .top) {
            
            Button(
                action: {
                    navigation.fullScreen(withAnimation: false) {
                        EventFormFullScreen(
                            initEventDb: eventUi.eventDb,
                            initText: nil,
                            initTime: nil,
                            onDone: {}
                        )
                    }
                },
                label: {
                    
                    VStack {
                        
                        HStack {
                            
                            Text(eventUi.dateString)
                                .font(.system(size: 14, weight: .light))
                                .foregroundColor(.secondary)
                            
                            Spacer()
                            
                            Text(eventUi.dayLeftString)
                                .font(.system(size: 14, weight: .light))
                                .foregroundColor(.secondary)
                        }
                        
                        HStack {
                            
                            Text(eventUi.listText)
                                .textAlign(.leading)
                            
                            Spacer()
                            
                            TriggersIconsView(
                                checklistsDb: eventUi.textFeatures.checklists,
                                shortcutsDb: eventUi.textFeatures.shortcuts
                            )
                        }
                        .padding(.top, 4)
                    }
                    .foregroundColor(.primary)
                    .padding(.top, 10)
                    .padding(.bottom, 10)
                }
            )
            
            if withTopDivider {
                Divider()
            }
        }
    }
}
