import SwiftUI
import shared

let HomeScreen__primaryFontSize: CGFloat = 18

let HomeScreen__hPadding: CGFloat = 8
let HomeScreen__itemHeight: CGFloat = 38
let HomeScreen__itemCircleHPadding: CGFloat = 7
let HomeScreen__itemCircleHeight: CGFloat = 24
let HomeScreen__itemCircleFontSize: CGFloat = 15
let HomeScreen__itemCircleFontWeight: Font.Weight = .semibold
let HomeScreen__itemCircleMarginTrailing: CGFloat = 8
let HomeScreen__secondaryColor: Color = MainTabsVm.companion.menuSecondaryColorDark.toColor()

struct HomeScreen: View {
    
    var body: some View {
        VmView({
            HomeVm()
        }) { vm, state in
            HomeScreenInner(
                vm: vm,
                state: state
            )
        }
    }
}

///

private struct HomeScreenInner: View {
    
    let vm: HomeVm
    let state: HomeVm.State
    
    ///
    
    @Environment(Navigation.self) private var navigation
    
    var body: some View {
        
        VStack {
            
            let checklistDb: ChecklistDb? = state.checklistDb
            
            HomeTimerView(vm: vm, state: state)
            
            if let whatsNewMessage = state.whatsNewMessage {
                Button(
                    action: {
                        navigation.push(.whatsNew)
                    },
                    label: {
                        Text(whatsNewMessage)
                            .foregroundColor(.white)
                            .padding(.horizontal, 12)
                            .padding(.vertical, 8)
                            .font(.system(size: 17, weight: .medium))
                            .background(roundedShape.fill(.red))
                            .padding(.top, 8)
                    }
                )
            }
            
            let isMainTasksExists = !state.mainTasks.isEmpty
            
            GeometryReader { geometry in
                
                let _ = vm.upListsContainerSize(
                    totalHeight: Float(geometry.size.height),
                    itemHeight: Float(HomeScreen__itemHeight)
                )
                
                VStack {
                    
                    if let checklistDb = checklistDb {
                        VStack {
                            ChecklistView(
                                checklistDb: checklistDb,
                                maxLines: 1,
                                withAddButton: false,
                                onDelete: {}
                            )
                        }
                        .frame(height: CGFloat(state.listsSizes.checklist))
                    }
                    
                    if isMainTasksExists {
                        HomeTasksView(
                            tasks: state.mainTasks
                        )
                        .frame(height: CGFloat(state.listsSizes.mainTasks))
                    }
                    
                    Spacer()
                }
            }
            
            if state.showReadme {
                HomeReadmeView(
                    title: state.readmeTitle,
                    buttonText: state.readmeButtonText,
                    onButtonClick: {
                        vm.onReadmeOpen()
                    }
                )
            }
            
            HomeButtonsView()
            
            Padding(vertical: 10.0)
        }
        .padding(.bottom, MainTabsView__HEIGHT)
    }
}
